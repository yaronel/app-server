package com.appsflyer.rta.httpserver;

public class EventLoopConfiguration
{
  private final String name;
  private final int threadCount;
  
  public EventLoopConfiguration(int threadCount)
  {
    this(threadCount, "NioEventLoop");
  }
  
  @SuppressWarnings("WeakerAccess")
  public EventLoopConfiguration(int threadCount, String name)
  {
    this.name = name;
    this.threadCount = threadCount;
  }
  
  public final String name()
  {
    return name;
  }
  
  public final int threadCount()
  {
    return threadCount;
  }
  
}
