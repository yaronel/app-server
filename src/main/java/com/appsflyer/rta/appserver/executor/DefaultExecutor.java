package com.appsflyer.rta.appserver.executor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.ThreadFactory;

public final class DefaultExecutor
{
  private DefaultExecutor() {}
  
  static ThreadFactory newThreadFactory(ExecutorConfig config)
  {
    return new DefaultThreadFactory(config.name(), true);
  }
  
  static EventLoopGroup newEventLoopGroup(ExecutorConfig config)
  {
    return new NioEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  public static EventExecutorGroup newEventExecutorGroup(ExecutorConfig config)
  {
    return new DefaultEventExecutorGroup(config.threadCount(), newThreadFactory(config));
  }
}
