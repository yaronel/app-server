package com.appsflyer.af_netty.util;

import com.appsflyer.af_netty.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

public interface NativeEventLoop
{
  EventLoopGroup newEventLoopGroup(EventLoopConfiguration config);
  
  Class<? extends ServerChannel> channelClass();
  
  default ThreadFactory newThreadFactory(EventLoopConfiguration config)
  {
    return new DefaultThreadFactory(config.name(), true);
  }
}
