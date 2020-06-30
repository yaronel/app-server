package com.appsflyer.rta.httpserver;

public final class EventLoopGroupConfig implements EventExecutorsConfig
{
  private final String name;
  private final int threadCount;
  
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
