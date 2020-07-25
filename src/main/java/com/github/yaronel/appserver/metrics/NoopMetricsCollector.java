package com.github.yaronel.appserver.metrics;

class NoopMetricsCollector implements MetricsCollector
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
  public void recordSendLatency(long nanos)
  {
  }
  
  @Override
  public void markSentBytes(long n)
  {
  }
  
  @Override
  public void recordReceiveLatency(long nanos)
  {
  }
  
  @Override
  public void recordResponseLatency(long nanos)
  {
  }
  
  @Override
  public void recordServiceLatency(long nanos)
  {
  }
  
  @Override
  public void markBytesReceived(long n)
  {
  }
}
