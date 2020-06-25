package com.appsflyer.af_netty.util;

import java.time.Duration;

public interface MetricsCollector
{
  void markHit();
  
  void markSuccessHit();
  
  void markErrorHit();
  
  void recordSendLatency(Duration duration);
  
  void markSentBytes(long n);
  
  void recordReceiveLatency(Duration duration);
  
  void recordResponseLatency(Duration duration);
  
  void recordServiceLatency(Duration duration);
  
  void markBytesReceived(long n);
}
