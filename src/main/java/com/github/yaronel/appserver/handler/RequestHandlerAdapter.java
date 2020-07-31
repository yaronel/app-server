package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AsciiString;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;

public abstract class RequestHandlerAdapter extends ChannelInboundHandlerAdapter
{
  private static final byte[] INTERNAL_SERVER_ERROR =
      AsciiString.of("Internal Server Error").toByteArray();
  private static final Map<String, String> INTERNAL_SERVER_ERROR_HEADERS =
      Collections.unmodifiableMap(Map.of(CONTENT_TYPE.toString(), TEXT_PLAIN.toString()));
  
  abstract Logger logger();
  
  void logException(Throwable cause)
  {
    Logger logger = logger();
    logger.error("Unhandled exception", cause);
    Throwable[] suppressed = cause.getSuppressed();
    if (suppressed.length > 0) {
      var msg = new StringJoiner(System.lineSeparator());
      msg.add("Printing suppressed exceptions:");
      for (int i = 0; i < suppressed.length; i++) {
        msg.add(String.format("Suppressed %d/%d: %s", i + 1, suppressed.length, suppressed[i].getMessage()));
      }
      logger.error(msg.toString());
    }
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    logException(cause);
    ctx.writeAndFlush(createServerError());
  }
  
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
  {
    if (evt instanceof IdleStateEvent) {
      ctx.close();
    }
  }
  
  private HttpResponse createServerError()
  {
    return HttpResponse.newInstance(
        500,
        INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR_HEADERS);
  }
}
