package com.appsflyer.af_netty.handler;

import com.appsflyer.af_netty.request.HttpRequest;
import io.netty.buffer.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

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
  
  private FullHttpRequest newRequest(byte[] content)
  {
    return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                      HttpMethod.GET,
                                      "http://example.com",
                                      Unpooled.wrappedBuffer(content));
  }
  
  /**
   * Creates a new {@link FullHttpRequest} with the given content,
   * and writes it into the channel.
   * Asserts that the request has been processed through the pipeline.
   */
  private void writeInbound(FullHttpRequest request)
  {
    assertTrue(channel.writeInbound(request));
    
  }
  
  private byte[] intToBytes(int n)
  {
    return new UnpooledHeapByteBuf(
        ByteBufAllocator.DEFAULT, 4, 4)
        .writeInt(n)
        .array();
  }
  
  private void validateContentDecoded(byte[] content) throws IOException
  {
    writeInbound(newRequest(content));
    
    HttpRequest request = channel.readInbound();
    
    assertEquals(new String(content), request.asString());
    assertArrayEquals(content, request.asBytes());
    assertArrayEquals(content, request.asStream().readAllBytes());
    
    request.recycle();
  }
  
  @Test
  void decodesFullHtmlRequestIntoHttpRequest()
  {
    writeInbound(newRequest(new byte[0]));
    var output = channel.readInbound();
    assertThat(output, instanceOf(HttpRequest.class));
    
    ((HttpRequest) output).recycle();
  }
  
  @Test
  void decodesContent() throws IOException
  {
    validateContentDecoded(new byte[0]);
    validateContentDecoded(intToBytes(Integer.MAX_VALUE));
    validateContentDecoded(intToBytes(Integer.MIN_VALUE));
    validateContentDecoded("OK".getBytes());
    validateContentDecoded("".getBytes());
    validateContentDecoded(" ".getBytes());
    validateContentDecoded("\uD83D\uDE00 \uD83D\uDE03 \uD83D\uDE04".getBytes());
    validateContentDecoded(
        ("ᚠᛇᚻ᛫ᛒᛦᚦ᛫ᚠᚱᚩᚠᚢᚱ᛫ᚠᛁᚱᚪ᛫ᚷᛖᚻᚹᛦᛚᚳᚢᛗ" +
            "ᛋᚳᛖᚪᛚ᛫ᚦᛖᚪᚻ᛫ᛗᚪᚾᚾᚪ᛫ᚷᛖᚻᚹᛦᛚᚳ᛫ᛗᛁᚳᛚᚢᚾ᛫ᚻᛦᛏ᛫ᛞᚫᛚᚪᚾ" +
            "ᚷᛁᚠ᛫ᚻᛖ᛫ᚹᛁᛚᛖ᛫ᚠᚩᚱ᛫ᛞᚱᛁᚻᛏᚾᛖ᛫ᛞᚩᛗᛖᛋ᛫ᚻᛚᛇᛏᚪᚾ᛬").getBytes());
  }
  
  @Test
  void decodesHeaders()
  {
    var headers = new DefaultHttpHeaders().add(ACCEPT, HttpHeaderValues.TEXT_HTML)
                                          .addInt(AGE, 86000)
                                          .addShort(DNT, (short) 1)
                                          .add(HOST, "com.example.app")
                                          .add(LOCATION, "com.example.login");
    
    FullHttpRequest request = newRequest(new byte[0]);
    request.headers().add(headers);
    writeInbound(request);
    
    HttpRequest output = channel.readInbound();
    Map<String, String> decodedHeaders = output.headers().getAll();
    
    headers.entries().forEach(
        entry -> assertEquals(entry.getValue(), decodedHeaders.get(entry.getKey())));
    
    output.recycle();
  }
  
  @Test
  void decodesEmptyHeaders()
  {
    writeInbound(newRequest(new byte[0]));
    HttpRequest output = channel.readInbound();
    assertTrue(output.headers().getAll().isEmpty());
    output.recycle();
  }
}
