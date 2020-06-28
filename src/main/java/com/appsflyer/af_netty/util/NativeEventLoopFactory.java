package com.appsflyer.af_netty.util;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;

public final class NativeEventLoopFactory
{
  private NativeEventLoopFactory() {}
  
  public static NativeEventLoop createInstance()
  {
    if (Epoll.isAvailable()) {
      return new EpollEventLoop();
    }
    else if (KQueue.isAvailable()) {
      return new KQueueFactory();
    }
    else {
      return new NioFactory();
    }
  }
}
