package com.appsflyer.af_netty;

public class EventLoopConfiguration
{
  private final String name;
  private final int threadCount;
  
  public EventLoopConfiguration(int threadCount)
  {
    this(threadCount, "NioEventLoop");
  }
  
  public EventLoopConfiguration(int threadCount, String name)
  {
    this.name = name;
    this.threadCount = threadCount;
  }
  
  public String name()
  {
    return name;
  }
  
  public int threadCount()
  {
    return threadCount;
  }
  
}
