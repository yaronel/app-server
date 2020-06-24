package com.appsflyer.af_netty.util;

import com.appsflyer.af_netty.EventLoopConfiguration;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NioUtil extends NativeSocketUtil
{
  @Override
  public EventLoopGroup newEventLoopGroup(EventLoopConfiguration config)
  {
    return new NioEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public Class<? extends ServerChannel> socketChannelClass()
  {
    return NioServerSocketChannel.class;
  }
}
