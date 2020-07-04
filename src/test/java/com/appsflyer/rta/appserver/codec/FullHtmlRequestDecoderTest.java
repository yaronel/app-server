package com.appsflyer.rta.appserver.codec;

import com.appsflyer.rta.appserver.HttpRequest;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("StringConcatenation")
class FullHtmlRequestDecoderTest
{
  private static final byte[] EMPTY_BYTES = new byte[0];
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
  
  private static byte[] intToBytes(int n)
  {
    return new UnpooledHeapByteBuf(
        ByteBufAllocator.DEFAULT, 4, 4)
        .writeInt(n)
        .array();
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
    validateContentDecoded("\uD83D\uDE00 \uD83D\uDE03 \uD83D\uDE04".getBytes(UTF_16));
    validateContentDecoded(
        ("ᚠᛇᚻ᛫ᛒᛦᚦ᛫ᚠᚱᚩᚠᚢᚱ᛫ᚠᛁᚱᚪ᛫ᚷᛖᚻᚹᛦᛚᚳᚢᛗ" +
            "ᛋᚳᛖᚪᛚ᛫ᚦᛖᚪᚻ᛫ᛗᚪᚾᚾᚪ᛫ᚷᛖᚻᚹᛦᛚᚳ᛫ᛗᛁᚳᛚᚢᚾ᛫ᚻᛦᛏ᛫ᛞᚫᛚᚪᚾ" +
            "ᚷᛁᚠ᛫ᚻᛖ᛫ᚹᛁᛚᛖ᛫ᚠᚩᚱ᛫ᛞᚱᛁᚻᛏᚾᛖ᛫ᛞᚩᛗᛖᛋ᛫ᚻᛚᛇᛏᚪᚾ᛬").getBytes(UTF_16));
  }
  
  @Test
  void decodesHeaderValuesAsString()
  {
    var headers = new DefaultHttpHeaders().add(ACCEPT, HttpHeaderValues.TEXT_HTML)
                                          .addInt(AGE, 86000)
                                          .addShort(DNT, (short) 1)
                                          .add(HOST, "com.example.app")
                                          .add(LOCATION, "com.example.login");
    
    FullHttpRequest request = newRequest(EMPTY_BYTES);
    request.headers().add(headers);
    writeInbound(request);
    
    com.appsflyer.rta.appserver.HttpRequest output = channel.readInbound();
    Map<String, String> decodedHeaders = output.headers();
    
    headers.entries().forEach(
        entry -> assertEquals(entry.getValue(), decodedHeaders.get(entry.getKey())));
    
    output.recycle();
  }
  
  @Test
  void decodesEmptyHeaders()
  {
    writeInbound(newRequest(EMPTY_BYTES));
    HttpRequest output = channel.readInbound();
    assertTrue(output.headers().isEmpty());
    output.recycle();
  }
}
