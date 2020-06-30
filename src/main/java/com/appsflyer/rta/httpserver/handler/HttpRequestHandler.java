package com.appsflyer.rta.httpserver.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(HttpRequestHandler.class);
  
  private static void logException(Throwable cause)
  {
    logger.error(cause.getMessage());
    Throwable[] suppressed = cause.getSuppressed();
    if (suppressed.length > 0) {
      logger.error("Printing suppressed exceptions:");
      for (int i = 0; i < suppressed.length; i++) {
        //noinspection HardcodedFileSeparator
        logger.error("Suppressed {}/{}: {}", i + 1, suppressed.length, suppressed[i].getMessage());
      }
    }
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    logException(cause);
    ctx.fireExceptionCaught(cause);
  }
}
