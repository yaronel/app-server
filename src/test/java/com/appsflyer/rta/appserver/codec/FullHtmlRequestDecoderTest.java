package com.appsflyer.rta.appserver.codec;

import com.appsflyer.rta.appserver.HttpRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static com.appsflyer.rta.appserver.TestUtil.*;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("StringConcatenation")
class FullHtmlRequestDecoderTest
{
  private EmbeddedChannel channel;
  
  @BeforeEach
  void setUp()
  {
    channel = new EmbeddedChannel(FullHtmlRequestDecoder.INSTANCE);
  }
  
  @AfterEach
  void tearDown()
  {
    channel.finishAndReleaseAll();
  }
  
  private static FullHttpRequest newRequest(byte[] content)
  {
    return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                      HttpMethod.GET,
                                      "http://example.com",
                                      Unpooled.wrappedBuffer(content));
  }
  
  /**
   * Writes the request inbound into the channel.
   * Asserts that the request has been processed through the pipeline.
   */
  private void writeInbound(FullHttpRequest request)
  {
    assertTrue(channel.writeInbound(request));
  }
  
  private void validateContentDecoded(byte[] content) throws IOException
  {
    writeInbound(newRequest(content));
    
    var output = channel.readInbound();
    assertThat(output, instanceOf(com.appsflyer.rta.appserver.HttpRequest.class));
    
    var request = (com.appsflyer.rta.appserver.HttpRequest) output;
    assertEquals(new String(content, UTF_8), request.asString());
    assertArrayEquals(content, request.asBytes());
    /*
    Note: We are closing the stream in the tearDown.
    In real life the proper way to use the stream is:
    (try InputStream = request.asStream()) {
      .... do something with the stream
    }
     */
    //noinspection resource
    assertArrayEquals(content, request.asStream().readAllBytes());
    
    request.recycle();
  }
  
  @Test
  void decodesContent() throws IOException
  {
    validateContentDecoded(EMPTY_BYTES);
    validateContentDecoded(intToBytes(Integer.MAX_VALUE));
    validateContentDecoded(intToBytes(Integer.MIN_VALUE));
    validateContentDecoded("OK".getBytes(UTF_8));
    validateContentDecoded("".getBytes(UTF_8));
    validateContentDecoded(" ".getBytes(UTF_8));
    validateContentDecoded(EMOJIS);
    validateContentDecoded(UTF_16_CHARACTERS);
  }
  
  @Test
  void decodesHeaderNamesToLowerCase()
  {
    var headers = new DefaultHttpHeaders()
        .add(ACCEPT, HttpHeaderValues.TEXT_HTML)
        .addInt(AGE, 86000)
        .addShort(DNT, (short) 1)
        .add(HOST, "com.example.app")
        .add(LOCATION, "com.example.login");
    
    FullHttpRequest request = newRequest(EMPTY_BYTES);
    request.headers().add(headers);
    
    validateHeadersDecoded(request);
  }
  
  @Test
  void decodesEmptyHeaders()
  {
    validateHeadersDecoded(newRequest(EMPTY_BYTES));
  }
  
  private void validateHeadersDecoded(FullHttpRequest original)
  {
    HttpHeaders originalHeaders = original.headers();
    
    writeInbound(original);
    
    var output = (HttpRequest) channel.readInbound();
    Map<String, String> decodedHeaders = output.headers();
    
    assertEquals(originalHeaders.size(), decodedHeaders.size());
    
    for (Map.Entry<String, String> header : originalHeaders.entries()) {
      String name = header.getKey().toLowerCase(Locale.ENGLISH);
      assertEquals(header.getValue(), decodedHeaders.get(name));
    }
    output.recycle();
  }
}
