package com.appsflyer.af_netty.util;

import com.af.metricsng.Metrics;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.time.Duration;

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
  
  public MetricsCollectorImpl(String metricPrefix)
  {
    this(metricPrefix, Metrics.getDefaultMetricRegistry());
  }
  
  public MetricsCollectorImpl(String metricPrefix, MetricRegistry registry)
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
  public void recordSendLatency(Duration duration)
  {
    sendLatency.update(duration.toNanos(), NANOSECONDS);
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
  public void recordReceiveLatency(Duration duration)
  {
    receiveLatency.update(duration.toNanos(), NANOSECONDS);
  }
  
  @Override
  public void recordResponseLatency(Duration duration)
  {
    responseLatency.update(duration.toNanos(), NANOSECONDS);
  }
  
  @Override
  public void recordServiceLatency(Duration duration)
  {
    serviceLatency.update(duration.toNanos(), NANOSECONDS);
  }
  
}
