package com.appsflyer.af_netty.handler;

import com.appsflyer.af_netty.HttpResponse;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@ChannelHandler.Sharable
public class FullHtmlResponseEncoder extends MessageToMessageEncoder<HttpResponse>
{
  public static final FullHtmlResponseEncoder INSTANCE = new FullHtmlResponseEncoder();
  
  @Override
  protected void encode(ChannelHandlerContext ctx, HttpResponse msg, List<Object> out)
  {
    var content = msg.content();
    FullHttpResponse response =
        new DefaultFullHttpResponse(
            msg.protocol(),
            HttpResponseStatus.valueOf(msg.statusCode()),
            PooledByteBufAllocator.DEFAULT.directBuffer(content.length).writeBytes(content),
            false);
    
    out.add(setHeaders(response, msg.headers()));
    msg.recycle();
  }
  
  /**
   * Sets headers of the {@code response}.
   *
   * @param response The response that will be written back to the client
   * @param headers  Map of name value pairs
   */
  private FullHttpResponse setHeaders(FullHttpResponse response, Map<String, String> headers)
  {
    HttpHeaders outHeaders = response.headers();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      outHeaders.set(entry.getKey(), entry.getValue());
    }
    outHeaders.set(CONTENT_LENGTH, response.content().readableBytes());
    return response;
  }
}
