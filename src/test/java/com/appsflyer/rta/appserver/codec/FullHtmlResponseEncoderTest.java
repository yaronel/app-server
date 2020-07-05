package com.appsflyer.rta.appserver.codec;

import com.appsflyer.rta.appserver.HttpResponse;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.appsflyer.rta.appserver.TestUtil.*;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

class FullHtmlResponseEncoderTest
{
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
  
  @Test
  void encodesHttpResponseIntoFullHttpResponse()
  {
    validateContentEncoded(newResponse(EMPTY_BYTES));
    validateContentEncoded(newResponse(intToBytes(Integer.MAX_VALUE)));
    validateContentEncoded(newResponse(intToBytes(Integer.MIN_VALUE)));
    validateContentEncoded(newResponse("OK".getBytes(UTF_8)));
    validateContentEncoded(newResponse("".getBytes(UTF_8)));
    validateContentEncoded(newResponse(" ".getBytes(UTF_8)));
    validateContentEncoded(newResponse(EMOJIS));
    validateContentEncoded(newResponse(UTF_16_CHARACTERS));
  }
  
  private void validateContentEncoded(HttpResponse original)
  {
    /*
     * Note: We need to capture the instance state because it will be recycled
     * after it's encoded.
     */
    byte[] content = original.content();
    int statusCode = original.statusCode();
    
    writeOutbound(original);
    
    var rawOutput = channel.readOutbound();
    assertThat(rawOutput, instanceOf(FullHttpResponse.class));
    var output = (FullHttpResponse) rawOutput;
    
    assertArrayEquals(content, ByteBufUtil.getBytes(output.content()));
  }
  
  @Test
  void decodesHeaderValuesAsString()
  {
    Map<String, String> headers = Map.ofEntries(
        Map.entry(ACCEPT.toString(), HttpHeaderValues.TEXT_HTML.toString()),
        Map.entry(AGE.toString(), "86000"),
        Map.entry(DNT.toString(), ""),
        Map.entry(HOST.toString(), "com.example.app"),
        Map.entry(LOCATION.toString(), "com.example.login"));
    
    var response = HttpResponse.newInstance(200, EMPTY_BYTES, headers);
    
    validateHeadersEncoded(response);
  }
  
  private void validateHeadersEncoded(HttpResponse original)
  {
    Map<String, String> headers = original.headers();
    
    writeOutbound(original);
    
    var rawOutput = (HttpMessage) channel.readOutbound();
    
    /* There may have been headers added to the response, but all the
     * original headers should be included
     */
    for (Map.Entry<String, String> header : headers.entrySet()) {
      assertEquals(header.getValue(), rawOutput.headers().get(header.getKey()));
    }
  }
  
  @Test
  void encodesStatusCode()
  {
    validateStatusCodeEncoded(HttpResponse.newInstance(200, EMPTY_BYTES));
    validateStatusCodeEncoded(HttpResponse.newInstance(304, EMPTY_BYTES));
    validateStatusCodeEncoded(HttpResponse.newInstance(404, EMPTY_BYTES));
    validateStatusCodeEncoded(HttpResponse.newInstance(500, EMPTY_BYTES));
  }
  
  private void validateStatusCodeEncoded(HttpResponse original)
  {
    int expected = original.statusCode();
    writeOutbound(original);
    var rawOutput = (FullHttpResponse) channel.readOutbound();
    assertEquals(expected, rawOutput.status().code());
  }
}
