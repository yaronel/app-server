package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.metrics.ManualClock;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.MetricsCollectorImpl;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.yaronel.appserver.TestUtil.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ServerMetricsHandlerTest
{
  private EmbeddedChannel channel;
  private ManualClock timeProvider;
  private MetricsCollector collector;
  
  @BeforeEach
  void setUp()
  {
    channel = new EmbeddedChannel();
    timeProvider = new ManualClock();
    // After the first time it's called, advance the clock by 1.
    timeProvider.onTime(instance -> {
      if (instance.calls() > 0) {
        instance.advanceClock();
      }
    });
    collector = mock(MetricsCollectorImpl.class);
    channel.pipeline().addLast(new ServerMetricsHandler(collector, timeProvider));
  }
  
  @AfterEach
  void tearDown()
  {
    channel.finishAndReleaseAll();
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
    timeProvider.advanceClock(5); // 5
    assertTrue(channel.writeInbound(bufferContent("foo")));
    timeProvider.advanceClock(3); // 8
    assertTrue(channel.writeInbound(bufferContent("bar")));
    timeProvider.advanceClock(7); // 15
    assertTrue(channel.writeInbound(lastHttpContent("")));
    // Clock should now be on 16
    timeProvider.advanceClock(); // Should not have an affect
  
    channel.readInbound();
    verify(collector).recordReceiveLatency(16L);
  }
  
  @Test
  void testReceiveLatencyOfIncompleteRequest()
  {
    assertTrue(channel.writeInbound(partialRequest()));
    timeProvider.advanceClock(); // 1
    assertTrue(channel.writeInbound(bufferContent("foo")));
    timeProvider.advanceClock(); // 2
  
    channel.readInbound();
  
    verify(collector, times(0)).recordReceiveLatency(anyLong());
  }
  
  @Test
  void testSendLatencyOfFullHttpResponse()
  {
    assertTrue(channel.writeOutbound(responseWithContent("")));
    timeProvider.advanceClock(10); // Should not have an effect
    channel.readOutbound();
    
    verify(collector).recordSendLatency(1L);
  }
  
  @Test
  void testSendLatencyOfMultipleHttpResponseParts()
  {
    timeProvider.advanceClock(); // 1
  
    assertTrue(channel.writeOutbound(partialResponse()));
    timeProvider.advanceClock(10); // 11
  
    assertTrue(channel.writeOutbound(bufferContent("foo")));
    timeProvider.advanceClock(6); // 17
    timeProvider.advanceClock(3); // 20
  
    assertTrue(channel.writeOutbound(bufferContent("bar")));
    timeProvider.advanceClock(4); // 24
  
    assertTrue(channel.writeOutbound(lastHttpContent("")));
    // Clock should be on 25
  
    timeProvider.advanceClock(20); // Should not have an effect
  
    channel.readOutbound();
  
    verify(collector).recordSendLatency(24L); // 25 end - 1 start
  }
  
  @Test
  void testSendLatencyOfIncompleteResponse()
  {
    timeProvider.advanceClock(10); // 10
    assertTrue(channel.writeOutbound(partialResponse()));
    assertTrue(channel.writeOutbound(bufferContent("foo")));
  
    timeProvider.advanceClock(); // 12
  
    assertTrue(channel.writeOutbound(bufferContent("foo")));
  
    timeProvider.advanceClock(); // 13
  
    channel.readOutbound();
  
    verify(collector, times(0)).recordSendLatency(anyLong());
  }
  
  @Test
  void testFullFlowLatencies_receive_send_response()
  {
    assertTrue(channel.writeInbound(requestWithContent(""))); // Receive = 1, Clock = 1
    timeProvider.advanceClock(5); // Clock = 6
    assertTrue(channel.writeOutbound(responseWithContent(""))); // Send = 1, Response = 2, Clock = 9
    timeProvider.advanceClock(); // Clock = 10
    channel.readOutbound();
    
    verify(collector).recordReceiveLatency(1L);
    verify(collector).recordSendLatency(1L);
    verify(collector).recordResponseLatency(2L);
  }
}
