package com.appsflyer.rta.httpserver;

public final class ExecutorsGroupConfig implements EventExecutorsConfig
{
  private final String name;
  private final int threadCount;
  
  /**
   * @return The default configuration with a thread count of 2 x number of processors.
   */
  public static EventExecutorsConfig defaultConfig()
  {
    return new ExecutorsGroupConfig(
        Runtime.getRuntime().availableProcessors() * 2);
  }
  
  @SuppressWarnings("WeakerAccess")
  public ExecutorsGroupConfig(int threadCount, String name)
  {
    this.name = name;
    this.threadCount = threadCount;
  }
  
  public ExecutorsGroupConfig(int threadCount)
  {
    this(threadCount, "AppThreadPool");
  }
  
  @Override
  public final String name()
  {
    return name;
  }
  
  @Override
  public final int threadCount()
  {
    return threadCount;
  }
  
}
