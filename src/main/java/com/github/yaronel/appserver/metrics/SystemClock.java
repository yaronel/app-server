package com.github.yaronel.appserver.metrics;

public class SystemClock implements TimeProvider
{
  /**
   * @return The current system nano time as described by {@link System#nanoTime()}
   */
  @Override
  public long time()
  {
    return System.nanoTime();
  }
}
