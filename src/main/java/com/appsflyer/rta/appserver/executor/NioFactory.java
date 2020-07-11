package com.appsflyer.rta.appserver.executor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

@SuppressWarnings("MethodReturnAlwaysConstant")
public class NioFactory extends AbstractEventLoopFactory
{
  @Override
  public final EventLoopGroup newGroup(ExecutorConfig config)
  {
    return DefaultExecutor.newEventLoopGroup(config);
  }
  
  @Override
  public final Class<? extends ServerChannel> channelClass()
  {
    return NioServerSocketChannel.class;
  }
}
