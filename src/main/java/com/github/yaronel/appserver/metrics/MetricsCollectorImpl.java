package com.github.yaronel.appserver.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class MetricsCollectorImpl implements MetricsCollector
{
  private final Meter hits;
  private final Meter successHits;
  private final Meter errorHits;
  private final Meter sentBytes;
  private final Meter receivedBytes;
  private final Timer serviceLatency;
  private final Timer responseLatency;
  private final Timer sendLatency;
  private final Timer receiveLatency;
  
  MetricsCollectorImpl(String metricPrefix)
  {
    this(metricPrefix, new MetricRegistry());
  }
  
  MetricsCollectorImpl(String metricPrefix, MetricRegistry registry)
  {
    hits = registry.meter(MetricRegistry.name(metricPrefix, "hits", "all"));
    successHits = registry.meter(MetricRegistry.name(metricPrefix, "hits", "success"));
    errorHits = registry.meter(MetricRegistry.name(metricPrefix, "hits", "error"));
    sentBytes = registry.meter(MetricRegistry.name(metricPrefix, "bytes", "sent"));
    receivedBytes = registry.meter(MetricRegistry.name(metricPrefix, "bytes", "received"));
    serviceLatency = registry.timer(MetricRegistry.name(metricPrefix, "latency", "service"));
    responseLatency = registry.timer(MetricRegistry.name(metricPrefix, "latency", "response"));
    sendLatency = registry.timer(MetricRegistry.name(metricPrefix, "latency", "send"));
    receiveLatency = registry.timer(MetricRegistry.name(metricPrefix, "latency", "receive"));
  }
  
  @Override
  public void markHit()
  {
    hits.mark();
  }
  
  @Override
  public void markSuccessHit()
  {
    successHits.mark();
  }
  
  @Override
  public void markErrorHit()
  {
    errorHits.mark();
  }
  
  @Override
  public void recordSendLatency(long nanos)
  {
    sendLatency.update(nanos, NANOSECONDS);
  }
  
  @Override
  public void markSentBytes(long n)
  {
    sentBytes.mark(n);
  }
  
  @Override
  public void markBytesReceived(long n)
  {
    receivedBytes.mark(n);
  }
  
  @Override
  public void recordReceiveLatency(long nanos)
  {
    receiveLatency.update(nanos, NANOSECONDS);
  }
  
  @Override
  public void recordResponseLatency(long nanos)
  {
    responseLatency.update(nanos, NANOSECONDS);
  }
  
  @Override
  public void recordServiceLatency(long nanos)
  {
    serviceLatency.update(nanos, NANOSECONDS);
  }
  
}
