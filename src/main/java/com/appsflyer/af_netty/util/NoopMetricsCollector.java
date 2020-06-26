package com.appsflyer.af_netty.util;

import java.time.Duration;

public class NoopMetricsCollector implements MetricsCollector
{
  @Override
  public void markHit()
  {
  }
  
  @Override
  public void markSuccessHit()
  {
  }
  
  @Override
  public void markErrorHit()
  {
  }
  
  @Override
  public void recordSendLatency(Duration duration)
  {
  }
  
  @Override
  public void markSentBytes(long n)
  {
  }
  
  @Override
  public void recordReceiveLatency(Duration duration)
  {
  }
  
  @Override
  public void recordResponseLatency(Duration duration)
  {
  }
  
  @Override
  public void recordServiceLatency(Duration duration)
  {
  }
  
  @Override
  public void markBytesReceived(long n)
  {
  }
}
