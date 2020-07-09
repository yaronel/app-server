package com.appsflyer.rta.appserver;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class NioFactory extends AbstractEventLoopFactory
{
  @Override
  public final EventLoopGroup newGroup(EventExecutorsConfig config)
  {
    return new NioEventLoopGroup(config.threadCount(), newThreadFactory(config));
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return NioServerSocketChannel.class;
  }
}
