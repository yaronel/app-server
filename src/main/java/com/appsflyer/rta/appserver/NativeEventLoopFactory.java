package com.appsflyer.rta.appserver;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;

final class NativeEventLoopFactory
{
  private NativeEventLoopFactory() {}
  
  static NativeEventLoop createInstance()
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
