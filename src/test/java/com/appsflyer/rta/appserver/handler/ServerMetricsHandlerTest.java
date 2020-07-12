package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.metrics.ManualTimer;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import com.appsflyer.rta.appserver.metrics.MetricsCollectorImpl;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static com.appsflyer.rta.appserver.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ServerMetricsHandlerTest
{
  private EmbeddedChannel channel;
  private ManualTimer receiveTimer;
  private ManualTimer sendTimer;
  private MetricsCollector collector;
  
  @BeforeEach
  void setUp()
  {
    channel = new EmbeddedChannel();
    AtomicLong latency = new AtomicLong(0);
    receiveTimer = new ManualTimer(latency::getAndIncrement);
    sendTimer = new ManualTimer(latency::getAndIncrement);
    collector = mock(MetricsCollectorImpl.class);
    channel.pipeline()
           .addLast(new ServerMetricsHandler(collector, receiveTimer, sendTimer));
  }
  
  @AfterEach
  void tearDown()
  {
    channel.finishAndReleaseAll();
  }
  
  private void incrementTimer()
  {
    receiveTimer.tick();
  }
  
  @Test
  void testReceiveLatencyOfFullHttpRequest()
  {
    assertTrue(channel.writeInbound(requestWithContent("")));
    channel.readInbound();
    
    verify(collector).recordReceiveLatency(1L);
  }
  
  @Test
  void testReceiveLatencyOfMultipleHttpRequestParts()
  {
    assertTrue(channel.writeInbound(partialRequest()));
    incrementTimer(); // 2
    assertTrue(channel.writeInbound(partialRequest()));
    incrementTimer(); // 3
    assertTrue(channel.writeInbound(partialRequest()));
    incrementTimer(); // 4
    assertTrue(channel.writeInbound(lastHttpContent("")));
    
    channel.readInbound();
    verify(collector).recordReceiveLatency(4L);
  }
  
  @Test
  void testReceiveLatencyOfIncompleteRequest()
  {
    assertTrue(channel.writeInbound(partialRequest()));
    incrementTimer(); // 2
    assertTrue(channel.writeInbound(partialRequest()));
    
    channel.readInbound();
    
    verify(collector, times(0)).recordReceiveLatency(anyLong());
  }
  
  @Test
  void testSendLatencyOfFullHttpResponse()
  {
    assertTrue(channel.writeOutbound(responseWithContent("")));
    channel.readOutbound();
    
    verify(collector).recordSendLatency(1L);
  }
  
  @Test
  void testSendLatencyOfMultipleHttpResponseParts()
  {
    assertTrue(channel.writeOutbound(partialResponse()));
    assertTrue(channel.writeOutbound(partialResponse()));
    assertTrue(channel.writeOutbound(partialResponse()));
    assertTrue(channel.writeOutbound(lastHttpContent("")));
    
    channel.readOutbound();
    
    verify(collector).recordSendLatency(1L);
  }
  
  @Test
  void testSendLatencyOfIncompleteResponse()
  {
    assertTrue(channel.writeOutbound(partialResponse()));
    assertTrue(channel.writeOutbound(partialResponse()));
    assertTrue(channel.writeOutbound(partialResponse()));
    
    channel.readOutbound();
    
    verify(collector, times(0)).recordSendLatency(anyLong());
  }
  
  @Test
  void testResponseLatencyOfFullHttpResponse()
  {
    assertTrue(channel.writeInbound(requestWithContent("")));
    assertTrue(channel.writeOutbound(responseWithContent("")));
    channel.readOutbound();
    
    verify(collector).recordResponseLatency(1L);
  }
}
