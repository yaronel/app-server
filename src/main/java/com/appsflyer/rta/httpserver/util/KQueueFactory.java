package com.appsflyer.rta.httpserver.util;

import com.appsflyer.rta.httpserver.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;

public class KQueueFactory implements NativeEventLoop
{
  KQueueFactory() {}
  
  @Override
  public final EventLoopGroup newEventLoopGroup(EventLoopConfiguration config)
  {
    return new KQueueEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return KQueueServerSocketChannel.class;
  }
}
