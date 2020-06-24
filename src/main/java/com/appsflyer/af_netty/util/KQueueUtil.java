package com.appsflyer.af_netty.util;

import com.appsflyer.af_netty.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;

public class KQueueUtil extends NativeSocketUtil
{
  @Override
  public EventLoopGroup newEventLoopGroup(EventLoopConfiguration config)
  {
    return new KQueueEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public Class<? extends ServerChannel> socketChannelClass()
  {
    return KQueueServerSocketChannel.class;
  }
}
