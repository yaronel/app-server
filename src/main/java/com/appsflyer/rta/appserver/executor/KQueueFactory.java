package com.appsflyer.rta.appserver.executor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class KQueueFactory extends AbstractEventLoopFactory
{
  @Override
  public final EventLoopGroup newGroup(ExecutorConfig config)
  {
    return new KQueueEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return KQueueServerSocketChannel.class;
  }
}
