package com.appsflyer.rta.appserver.util;

import org.slf4j.Logger;

public final class HandlerUtil
{
  private HandlerUtil() {}
  
  public static void logException(Logger logger, Throwable cause)
  {
    logger.error("Unhandled exception", cause);
    Throwable[] suppressed = cause.getSuppressed();
    if (suppressed.length > 0) {
      logger.error("Printing suppressed exceptions:");
      for (int i = 0; i < suppressed.length; i++) {
        //noinspection HardcodedFileSeparator
        logger.error("Suppressed {}/{}: {}", i + 1, suppressed.length, suppressed[i].getMessage());
      }
    }
  }
}
