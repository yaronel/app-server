package com.appsflyer.af_netty.util;

import com.appsflyer.af_netty.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

public abstract class EventLoopFactory
{
  private static final EventLoopFactory INSTANCE = createInstance();
  
  public static EventLoopFactory getInstance()
  {
    return INSTANCE;
  }
  
  private static EventLoopFactory createInstance()
  {
    if (Epoll.isAvailable()) {
      return new EpollFactory();
    }
    else if (KQueue.isAvailable()) {
      return new KQueueFactory();
    }
    else {
      return new NioFactory();
    }
  }
  
  ThreadFactory newThreadFactory(EventLoopConfiguration config)
  {
    return new DefaultThreadFactory(config.name(), true);
  }
  
  public abstract EventLoopGroup newEventLoopGroup(EventLoopConfiguration config);
  
  public abstract Class<? extends ServerChannel> channelClass();
  
  
}
