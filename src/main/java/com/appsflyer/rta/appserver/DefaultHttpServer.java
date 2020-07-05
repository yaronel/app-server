package com.appsflyer.rta.appserver;

import com.appsflyer.rta.appserver.handler.HttpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public final class DefaultHttpServer implements HttpServer
{
  private static final Logger logger = LoggerFactory.getLogger(DefaultHttpServer.class);
  private static final int TCP_CONNECTION_QUEUE_SIZE = 8192;
  private static final EventExecutorsConfig bossGroupConfig = new EventLoopGroupConfig(1);
  
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
    bootstrap = new ServerBootstrap();
  }
  
  @Override
  public void start() throws InterruptedException, UnknownHostException
  {
    InetAddress host = InetAddress.getByName(config.host());
    NativeEventLoop eventLoopCreator = NativeEventLoopFactory.createInstance();
    serverChannel =
        bootstrap
            .option(ChannelOption.SO_BACKLOG, TCP_CONNECTION_QUEUE_SIZE)
            /* Good explanation on socket reuse address:
             * https://stackoverflow.com/questions/14388706/how-do-so-reuseaddr-and-so-reuseport-differ
             */
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.connectTimeout().toMillis())
            .group(eventLoopCreator.newGroup(bossGroupConfig),
                   eventLoopCreator.newGroup(config.childGroupConfig()))
            .channel(eventLoopCreator.channelClass())
            .childHandler(new HttpChannelInitializer(config))
            .bind(host, config.port())
            .sync()
            .channel();
    
    logger.info("Started HTTP server listening on port {}", config.port());
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
    if (!group.isShuttingDown() && !group.isShutdown()) {
      if (group.shutdownGracefully().awaitUninterruptibly(5L, TimeUnit.SECONDS)) {
        if (group.isTerminated()) {
          logger.info("Server is down");
        }
      }
    }
  }
}
