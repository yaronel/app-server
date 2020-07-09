package com.appsflyer.rta.appserver;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;

public abstract class AbstractEventLoopFactory implements NativeEventLoopFactory
{
  public static NativeEventLoopFactory newInstance()
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
}
