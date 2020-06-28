package com.appsflyer.rta.httpserver.util;

import com.appsflyer.rta.httpserver.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

public class EpollEventLoop implements NativeEventLoop
{
  EpollEventLoop() {}
  
  @Override
  public final EventLoopGroup newEventLoopGroup(EventLoopConfiguration config)
  {
    return new EpollEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return EpollServerSocketChannel.class;
  }
}
