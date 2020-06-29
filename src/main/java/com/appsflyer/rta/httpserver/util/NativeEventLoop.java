package com.appsflyer.rta.httpserver.util;

import com.appsflyer.rta.httpserver.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

@SuppressWarnings("MethodReturnAlwaysConstant")
public interface NativeEventLoop
{
  EventLoopGroup newGroup(EventLoopConfiguration config);
  
  Class<? extends ServerChannel> channelClass();
  
  default ThreadFactory newThreadFactory(EventLoopConfiguration config)
  {
    return new DefaultThreadFactory(config.name(), true);
  }
}
