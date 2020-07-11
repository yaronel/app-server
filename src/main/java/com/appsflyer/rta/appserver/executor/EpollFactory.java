package com.appsflyer.rta.appserver.executor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class EpollFactory extends AbstractEventLoopFactory
{
  @Override
  public final EventLoopGroup newGroup(ExecutorConfig config)
  {
    return new EpollEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return EpollServerSocketChannel.class;
  }
}
