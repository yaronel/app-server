package com.appsflyer.rta.appserver;

public final class ExecutorsGroupConfig implements EventExecutorsConfig
{
  private final String name;
  private final int threadCount;
  
  /**
   * @return The default configuration with a thread count of 2 x number of processors.
   */
  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  static EventExecutorsConfig defaultConfig()
  {
    return new ExecutorsGroupConfig(
        Runtime.getRuntime().availableProcessors() << 1);
  }
  
  @SuppressWarnings("WeakerAccess")
  public ExecutorsGroupConfig(int threadCount, String name)
  {
    this.name = name;
    this.threadCount = threadCount;
  }
  
  @SuppressWarnings("WeakerAccess")
  public ExecutorsGroupConfig(int threadCount)
  {
    this(threadCount, "AppThreadPool");
  }
  
  @Override
  public String name()
  {
    return name;
  }
  
  @Override
  public int threadCount()
  {
    return threadCount;
  }
  
}
