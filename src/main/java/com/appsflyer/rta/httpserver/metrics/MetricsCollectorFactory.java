package com.appsflyer.rta.httpserver.metrics;

import com.codahale.metrics.MetricRegistry;

public final class MetricsCollectorFactory
{
  public static final MetricsCollector NOOP = new NoopMetricsCollector();
  
  private MetricsCollectorFactory() {}
  
  public static MetricsCollector newInstance(String metricPrefix)
  {
    return new MetricsCollectorImpl(metricPrefix);
  }
  
  public static MetricsCollector newInstance(String metricPrefix, MetricRegistry registry)
  {
    return new MetricsCollectorImpl(metricPrefix, registry);
  }
}
