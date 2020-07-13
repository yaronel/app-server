package com.appsflyer.rta.appserver;

import com.appsflyer.rta.appserver.executor.AbstractEventLoopFactory;
import com.appsflyer.rta.appserver.executor.EventExecutorConfig;
import com.appsflyer.rta.appserver.executor.ExecutorConfig;
import com.appsflyer.rta.appserver.executor.NativeEventLoopFactory;
import com.appsflyer.rta.appserver.handler.HttpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class DefaultHttpServer implements HttpServer
{
  private static final Logger logger = LoggerFactory.getLogger(DefaultHttpServer.class);
  private static final int TCP_CONNECTION_QUEUE_SIZE = 8192;
  private static final ExecutorConfig bossGroupConfig = new EventExecutorConfig(1);
  
  private final ServerConfig config;
  private final ServerBootstrap bootstrap;
  private Channel serverChannel;
  
  public static HttpServer newInstance(ServerConfig config)
  {
    return new DefaultHttpServer(config);
  }
  
  private DefaultHttpServer(ServerConfig config)
  {
    this.config = config;
    bootstrap = bootstrap();
  }
  
  @Override
  public void start() throws InterruptedException
  {
    serverChannel = bootstrap.bind(config.port())
                             .sync()
                             .channel();
    
    logger.info("Started HTTP server listening on port {}", config.port());
  }
  
  private ServerBootstrap bootstrap()
  {
    NativeEventLoopFactory eventLoopCreator = AbstractEventLoopFactory.newInstance();
    ServerBootstrap serverBootstrap = new ServerBootstrap()
        .option(ChannelOption.SO_BACKLOG, TCP_CONNECTION_QUEUE_SIZE)
        /* Good explanation on socket reuse address:
         * https://stackoverflow.com/questions/14388706/how-do-so-reuseaddr-and-so-reuseport-differ
         */
        .option(ChannelOption.SO_REUSEADDR, true)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.connectTimeout().toMillis())
        .channel(eventLoopCreator.channelClass())
        .childHandler(new HttpChannelInitializer(config));
    
    if (config.childGroupConfig().threadCount() > 0) {
      serverBootstrap.group(eventLoopCreator.newGroup(bossGroupConfig),
                            eventLoopCreator.newGroup(config.childGroupConfig()));
    }
    else {
      serverBootstrap.group(eventLoopCreator.newGroup(bossGroupConfig));
    }
    
    return serverBootstrap;
  }
  
  /**
   * Blocks the calling thread until the server socket is closed.
   * Gracefully shuts down the event loop groups once the server socket is
   * closed or the thread is interrupted.
   */
  @Override
  public void awaitTermination()
  {
    if (serverChannel != null) {
      try {
        serverChannel.closeFuture().sync();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } finally {
        shutdownEventLoopGroup();
      }
    }
  }
  
  /**
   * Tries to close the server socket and gracefully shutdown the event loop groups.
   */
  @Override
  public void stop()
  {
    if (serverChannel != null) {
      try {
        logger.info("Shutting down server ...");
        serverChannel.close().await(5L, TimeUnit.SECONDS);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } finally {
        shutdownEventLoopGroup();
      }
    }
  }
  
  private void shutdownEventLoopGroup()
  {
    EventLoopGroup group = bootstrap.config().group();
    if (isShutdown(group)) {
      return;
    }
    if (group.shutdownGracefully().awaitUninterruptibly(5L, TimeUnit.SECONDS)) {
      logger.info("Server is down");
    }
  }
  
  private static boolean isShutdown(EventExecutorGroup group)
  {
    return group.isShuttingDown() || group.isShutdown();
  }
}
