package com.appsflyer.rta.appserver;

public final class EventLoopGroupConfig implements EventExecutorsConfig
{
  private final String name;
  private final int threadCount;
  
  /**
   * @return The default configuration with a thread count of 2 x number of processors.
   */
  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static EventLoopGroupConfig defaultConfig()
  {
    return new EventLoopGroupConfig(
        Runtime.getRuntime().availableProcessors() << 1);
  }
  
  @SuppressWarnings("WeakerAccess")
  public EventLoopGroupConfig(int threadCount, String name)
  {
    this.name = name;
    this.threadCount = threadCount;
  }
  
  public EventLoopGroupConfig(int threadCount)
  {
    this(threadCount, "NioEventLoop");
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
