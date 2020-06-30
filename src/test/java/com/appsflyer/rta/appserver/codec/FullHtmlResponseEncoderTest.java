package com.appsflyer.rta.appserver.codec;

import com.appsflyer.rta.appserver.HttpResponse;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

class FullHtmlResponseEncoderTest
{
  private static final byte[] EMPTY_BYTES = new byte[0];
  private EmbeddedChannel channel;
  
  @BeforeEach
  void setUp()
  {
    channel = new EmbeddedChannel(FullHtmlResponseEncoder.INSTANCE);
  }
  
  @AfterEach
  void tearDown()
  {
    channel.finishAndReleaseAll();
  }
  
  private static HttpResponse newResponse(byte[] content)
  {
    return HttpResponse.newInstance(200, content);
  }
  
  /**
   * Writes the response outbound into the channel.
   * Asserts that the response has been processed through the pipeline.
   */
  private void writeOutbound(HttpResponse response)
  {
    assertTrue(channel.writeOutbound(response));
    
  }
  
  private void validateResponseEncoded(HttpResponse original)
  {
    writeOutbound(original);
    var rawOutput = channel.readOutbound();
    assertThat(rawOutput, instanceOf(FullHttpResponse.class));
    var output = (FullHttpResponse) rawOutput;
    
    assertArrayEquals(original.content(), ByteBufUtil.getBytes(output.content()));
    assertSame(original.headers().entrySet(), output.headers().entries());
    
    original.recycle();
  }
  
  //  @Test
  void encodesHttpResponseIntoFullHttpResponse()
  {
    validateResponseEncoded(newResponse(EMPTY_BYTES));
    
  }
  
}
