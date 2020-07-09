package com.appsflyer.rta.appserver;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

@SuppressWarnings("MethodReturnAlwaysConstant")
public interface NativeEventLoopFactory
{
  EventLoopGroup newGroup(EventExecutorsConfig config);
  
  Class<? extends ServerChannel> channelClass();
  
  default ThreadFactory newThreadFactory(EventExecutorsConfig config)
  {
    return new DefaultThreadFactory(config.name(), true);
  }
}
