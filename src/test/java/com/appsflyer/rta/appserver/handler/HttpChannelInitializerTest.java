package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HandlerMode;
import com.appsflyer.rta.appserver.ServerConfig;
import com.appsflyer.rta.appserver.executor.EventExecutorConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.appsflyer.rta.appserver.handler.HttpChannelInitializer.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpChannelInitializerTest
{
  private EmbeddedChannel channel;
  private SocketChannel mockServerChannel;
  private ServerConfig config;
  
  @BeforeEach
  void setUp()
  {
    channel = new EmbeddedChannel();
    mockServerChannel = mock(SocketChannel.class);
    when(mockServerChannel.pipeline()).thenReturn(channel.pipeline());
    when(mockServerChannel.eventLoop()).thenReturn(channel.eventLoop());
    
    config = mock(ServerConfig.class);
    when(config.writeTimeout()).thenReturn(Duration.ZERO);
    when(config.mode()).thenReturn(HandlerMode.NON_BLOCKING);
  }
  
  @AfterEach
  void tearDown()
  {
    channel.finishAndReleaseAll();
  }
  
  @Test
  void createsDefaultChannelPipeline()
  {
    when(config.isCompress()).thenReturn(false);
    new HttpChannelInitializer(config).initChannel(mockServerChannel);
    
    assertDefaultHandlersExist(mockServerChannel.pipeline());
    
    assertNull(mockServerChannel.pipeline().get(DECOMPRESSOR));
    assertNull(mockServerChannel.pipeline().get(COMPRESSOR));
  }
  
  private static void assertDefaultHandlersExist(ChannelPipeline pipeline)
  {
    assertNotNull(pipeline.get(IDLE_STATE_HANDLER));
    assertNotNull(pipeline.get(SERVER_CODEC));
    assertNotNull(pipeline.get(METRICS_HANDLER));
    assertNotNull(pipeline.get(KEEP_ALIVE_HANDLER));
    assertNotNull(pipeline.get(AGGREGATOR_HANDLER));
    assertNotNull(pipeline.get(WRITE_TIMEOUT_HANDLER));
    assertNotNull(pipeline.get(REQUEST_DECODER));
    assertNotNull(pipeline.get(RESPONSE_ENCODER));
    assertNotNull(pipeline.get(APP_HANDLER));
  }
  
  @Test
  void addsCompressionCodecsWhenIndicatedByServerConfig()
  {
    when(config.isCompress()).thenReturn(true);
    new HttpChannelInitializer(config).initChannel(mockServerChannel);
    
    assertDefaultHandlersExist(mockServerChannel.pipeline());
    
    assertNotNull(mockServerChannel.pipeline().get(DECOMPRESSOR));
    assertNotNull(mockServerChannel.pipeline().get(COMPRESSOR));
  }
  
  @Test
  void appHandlerUsesDefaultExecutorWhenNotBlocking()
  {
    when(config.isBlockingIo()).thenReturn(false);
    new HttpChannelInitializer(config).initChannel(mockServerChannel);
    
    ChannelHandlerContext serverCodecContext = mockServerChannel.pipeline().context(SERVER_CODEC);
    ChannelHandlerContext appHandlerContext = mockServerChannel.pipeline().context(APP_HANDLER);
    
    assertEquals(serverCodecContext.executor(), appHandlerContext.executor());
    assertEquals(mockServerChannel.eventLoop(), appHandlerContext.executor());
  }
  
  @Test
  void createsNewExecutorInBlockingMode()
  {
    when(config.isBlockingIo()).thenReturn(true);
    when(config.blockingExecutorsConfig()).thenReturn(EventExecutorConfig.defaultConfig());
    new HttpChannelInitializer(config).initChannel(mockServerChannel);
    
    ChannelHandlerContext serverCodecContext = mockServerChannel.pipeline().context(SERVER_CODEC);
    ChannelHandlerContext appHandlerContext = mockServerChannel.pipeline().context(APP_HANDLER);
    
    assertEquals(mockServerChannel.eventLoop(), serverCodecContext.executor());
    assertNotEquals(serverCodecContext.executor(), appHandlerContext.executor());
  }
}

