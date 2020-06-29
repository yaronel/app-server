package com.appsflyer.rta.httpserver.util;

import com.appsflyer.rta.httpserver.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class EpollEventLoop implements NativeEventLoop
{
  EpollEventLoop() {}
  
  @Override
  public final EventLoopGroup newGroup(EventLoopConfiguration config)
  {
    return new EpollEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return EpollServerSocketChannel.class;
  }
}
