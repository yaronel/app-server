package com.appsflyer.af_netty.util;

import com.appsflyer.af_netty.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

public abstract class NativeSocketUtil
{
  private static NativeSocketUtil instance;
  private static final Object mutex = new Object();
  
  public static NativeSocketUtil getInstance()
  {
    return createOrGetInstance();
  }
  
  private static NativeSocketUtil createOrGetInstance()
  {
    if (instance == null) {
      synchronized (mutex) {
        if (instance == null) {
          instance = createInstance();
        }
      }
    }
    return instance;
  }
  
  private static NativeSocketUtil createInstance()
  {
    if (Epoll.isAvailable()) {
      return new EpollUtil();
    }
    else if (KQueue.isAvailable()) {
      return new KQueueUtil();
    }
    else {
      return new NioUtil();
    }
  }
  
  ThreadFactory newThreadFactory(EventLoopConfiguration config)
  {
    return new DefaultThreadFactory(config.name(), true);
  }
  
  public abstract EventLoopGroup newEventLoopGroup(EventLoopConfiguration config);
  
  public abstract Class<? extends ServerChannel> socketChannelClass();
  
  
}
