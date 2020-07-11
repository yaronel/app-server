package com.appsflyer.rta.appserver.executor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

import java.util.concurrent.ThreadFactory;

@SuppressWarnings("MethodReturnAlwaysConstant")
public interface NativeEventLoopFactory
{
  EventLoopGroup newGroup(ExecutorConfig config);
  
  Class<? extends ServerChannel> channelClass();
  
  default ThreadFactory newThreadFactory(ExecutorConfig config)
  {
    return DefaultExecutor.newThreadFactory(config);
  }
}
