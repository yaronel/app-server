package com.appsflyer.rta.appserver.metrics;

public interface MetricsCollector
{
  void markHit();
  
  void markSuccessHit();
  
  void markErrorHit();
  
  void recordSendLatency(long nanos);
  
  void markSentBytes(long n);
  
  void recordReceiveLatency(long nanos);
  
  void recordResponseLatency(long nanos);
  
  void recordServiceLatency(long nanos);
  
  void markBytesReceived(long n);
}
