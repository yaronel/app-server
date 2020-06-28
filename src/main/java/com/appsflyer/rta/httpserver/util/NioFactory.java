package com.appsflyer.rta.httpserver.util;

import com.appsflyer.rta.httpserver.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NioFactory implements NativeEventLoop
{
  NioFactory() {}
  
  @Override
  public final EventLoopGroup newEventLoopGroup(EventLoopConfiguration config)
  {
    return new NioEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return NioServerSocketChannel.class;
  }
}
