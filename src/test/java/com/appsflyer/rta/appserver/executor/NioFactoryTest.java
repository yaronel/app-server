package com.appsflyer.rta.appserver.executor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutor;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class NioFactoryTest
{
  private static int groupSize(Iterable<EventExecutor> group)
  {
    int i = 0;
    for (var iter = group.iterator(); iter.hasNext(); i++) {
      iter.next();
    }
    return i;
  }
  
  @Test
  void createsNioEventLoopGroup()
  {
    EventLoopGroup group = new NioFactory().newGroup(
        new EventExecutorConfig(2, "foo"));
    
    assertThat(group, instanceOf(NioEventLoopGroup.class));
    assertEquals(2, groupSize(group));
  }
  
  @Test
  void returnsNioChannel()
  {
    assertSame(NioServerSocketChannel.class, new NioFactory().channelClass());
  }
}
