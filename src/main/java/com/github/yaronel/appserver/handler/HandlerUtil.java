package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpResponse;
import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;

final class HandlerUtil
{
  private static final Logger logger = LoggerFactory.getLogger(HandlerUtil.class);
  private static final byte[] INTERNAL_SERVER_ERROR =
      AsciiString.of("Internal Server Error").toByteArray();
  private static final Map<String, String> INTERNAL_SERVER_ERROR_HEADERS =
      Collections.unmodifiableMap(Map.of(CONTENT_TYPE.toString(), TEXT_PLAIN.toString()));
  
  private HandlerUtil() {}
  
  static void logException(Throwable cause)
  {
    logger.error("Unhandled exception", cause);
    Throwable[] suppressed = cause.getSuppressed();
    if (suppressed.length > 0) {
      logger.error("Printing suppressed exceptions:");
      for (int i = 0; i < suppressed.length; i++) {
        logger.error("Suppressed {}/{}: {}", i + 1, suppressed.length, suppressed[i].getMessage());
      }
    }
  }
  
  static HttpResponse createServerError()
  {
    return HttpResponse.newInstance(
        500,
        INTERNAL_SERVER_ERROR,
        INTERNAL_SERVER_ERROR_HEADERS);
  }
}
