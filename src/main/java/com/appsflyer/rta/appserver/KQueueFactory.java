package com.appsflyer.rta.appserver;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class KQueueFactory extends AbstractEventLoopFactory
{
  @Override
  public final EventLoopGroup newGroup(EventExecutorsConfig config)
  {
    return new KQueueEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return KQueueServerSocketChannel.class;
  }
}
