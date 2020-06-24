package com.appsflyer.af_netty;

import com.appsflyer.af_netty.channel.HttpChannelInitializer;
import com.appsflyer.af_netty.channel.ChannelConfiguration;
import com.appsflyer.af_netty.util.NativeSocketUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class HttpServer
{
  private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
  private static final int TCP_CONNECTION_QUEUE_SIZE = 8192;
  
  private final ServerConfiguration config;
  private final NativeSocketUtil util;
  private final EventLoopGroup bossGroup;
  private Channel serverSocket;
  
  public HttpServer(ServerConfiguration config)
  {
    this.config = config;
    util = NativeSocketUtil.getInstance();
    bossGroup = util.newEventLoopGroup(config.bossGroupConfig());
  }
  
  public void start() throws InterruptedException, UnknownHostException
  {
    InetAddress host = InetAddress.getByName(config.host());
    
    serverSocket =
        new ServerBootstrap()
            .option(ChannelOption.SO_BACKLOG, TCP_CONNECTION_QUEUE_SIZE)
            /* Good explanation on socket reuse address:
             * https://stackoverflow.com/questions/14388706/how-do-so-reuseaddr-and-so-reuseport-differ
             */
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) config.connectTimeout().toMillis())
            .group(bossGroup, util.newEventLoopGroup(config.childGroupConfig()))
            .channel(util.socketChannelClass())
            .childHandler(getChannelInitializer())
            .bind(host, config.port())
            .sync()
            .channel();
    
    logger.info("Started HTTP server listening on port {}", config.port());
  }
  
  private HttpChannelInitializer getChannelInitializer()
  {
    return new HttpChannelInitializer(
        ChannelConfiguration
            .newBuilder()
            .setInboundHandler(new HttpServerHandler(config.requestHandler()))
            .compress(config.isCompress())
            .setReadTimeout(config.readTimeout())
            .setWriteTimeout(config.writeTimeout())
            .build());
  }
  
  /**
   * Blocks the calling thread until the server socket is closed.
   * Gracefully shuts down the event loop groups once the server socket is
   * closed or the thread is interrupted.
   */
  public void awaitTermination()
  {
    if (serverSocket != null) {
      try {
        logger.info("Shutting down server ...");
        serverSocket.closeFuture().sync();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      } finally {
        bossGroup.shutdownGracefully();
        logger.info("Server is down");
      }
    }
  }
  
  /**
   * Tries to close the server socket and gracefully shutdown the event loop groups.
   */
  public void stop()
  {
    try {
      logger.info("Shutting down server ...");
      serverSocket.close().await(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } finally {
      bossGroup.shutdownGracefully();
      logger.info("Server is down");
    }
  }
}
