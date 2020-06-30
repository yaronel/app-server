package com.appsflyer.rta.httpserver;

import com.appsflyer.rta.httpserver.channel.ChannelConfiguration;
import com.appsflyer.rta.httpserver.channel.HttpChannelInitializer;
import com.appsflyer.rta.httpserver.handler.*;
import com.appsflyer.rta.httpserver.util.NativeEventLoop;
import com.appsflyer.rta.httpserver.util.NativeEventLoopFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
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
  
  private final ServerConfiguration config;
  private final ServerBootstrap bootstrap;
  private Channel serverChannel;
  
  public static HttpServer newInstance(ServerConfiguration config)
  {
    return new DefaultHttpServer(config);
  }
  
  private DefaultHttpServer(ServerConfiguration config)
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
            .childHandler(getChannelInitializer())
            .bind(host, config.port())
            .sync()
            .channel();
    
    logger.info("Started HTTP server listening on port {}", config.port());
  }
  
  private HttpChannelInitializer getChannelInitializer()
  {
    var builder = new ChannelConfiguration.Builder()
        .setInboundHandler(newRequestHandler())
        .compress(config.isCompress())
        .setReadTimeout(config.readTimeout())
        .setWriteTimeout(config.writeTimeout())
        .setMetricsCollector(config.metricsCollector());
    
    if (config.isBlockingIo()) {
      builder.setEventExecutors(createEventExecutors());
    }
    
    return new HttpChannelInitializer(builder.build());
  }
  
  private EventExecutorGroup createEventExecutors()
  {
    EventExecutorsConfig executorsConfig = config.blockingExecutorsConfig();
    return new DefaultEventExecutorGroup(
        executorsConfig.threadCount(),
        new DefaultThreadFactory(executorsConfig.name()));
  }
  
  private HttpRequestHandler newRequestHandler()
  {
    if (config.isBlockingIo()) {
      return new SyncHttpRequestHandler(
          (SyncRequestHandler) config.requestHandler(),
          config.metricsCollector());
    }
    
    return new AsyncHttpRequestHandler(
        (AsyncRequestHandler) config.requestHandler(),
        config.metricsCollector());
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
        logger.info("Server is down");
      }
    }
  }
}
