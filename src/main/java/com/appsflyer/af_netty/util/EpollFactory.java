package com.appsflyer.af_netty.util;

import com.appsflyer.af_netty.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

public class EpollFactory extends EventLoopFactory
{
  @Override
  public EventLoopGroup newEventLoopGroup(EventLoopConfiguration config)
  {
    return new EpollEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public Class<? extends ServerChannel> channelClass()
  {
    return EpollServerSocketChannel.class;
  }
}
