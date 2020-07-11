package com.appsflyer.rta.appserver.executor;

public final class EventExecutorConfig implements ExecutorConfig
{
  private final String name;
  private final int threadCount;
  
  /**
   * @return The default configuration with a thread count equal to the number of processors.
   */
  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static EventExecutorConfig defaultConfig()
  {
    return new EventExecutorConfig(
        Runtime.getRuntime().availableProcessors());
  }
  
  @SuppressWarnings("WeakerAccess")
  public EventExecutorConfig(int threadCount, String name)
  {
    this.name = name;
    this.threadCount = threadCount;
  }
  
  public EventExecutorConfig(int threadCount)
  {
    this(threadCount, "EventExecutor");
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
