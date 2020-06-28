package com.appsflyer.rta.httpserver.channel;

import com.appsflyer.rta.httpserver.metrics.MetricsCollector;
import io.netty.channel.ChannelInboundHandler;

import java.time.Duration;
import java.util.Objects;

public final class ChannelConfiguration
{
  private int maxContentLength;
  private Duration readTimeout;
  private Duration writeTimeout;
  private ChannelInboundHandler inboundHandler;
  private boolean compress;
  private MetricsCollector metricsCollector;
  
  private ChannelConfiguration() {}
  
  int maxContentLength()
  {
    return maxContentLength;
  }
  
  Duration readTimeout()
  {
    return readTimeout;
  }
  
  Duration writeTimeout()
  {
    return writeTimeout;
  }
  
  ChannelInboundHandler inboundHandler()
  {
    return inboundHandler;
  }
  
  boolean isCompress()
  {
    return compress;
  }
  
  MetricsCollector metricsCollector()
  {
    return metricsCollector;
  }
  
  public static final class Builder
  {
    private ChannelConfiguration instance;
    
    public Builder()
    {
      this(new ChannelConfiguration());
    }
    
    private Builder(ChannelConfiguration instance) {this.instance = instance;}
    
    public Builder setInboundHandler(ChannelInboundHandler inboundHandler)
    {
      Objects.requireNonNull(inboundHandler);
      instance.inboundHandler = inboundHandler;
      return this;
    }
    
    public Builder setReadTimeout(Duration duration)
    {
      Objects.requireNonNull(duration);
      instance.readTimeout = duration;
      return this;
    }
    
    public Builder setWriteTimeout(Duration duration)
    {
      Objects.requireNonNull(duration);
      instance.writeTimeout = duration;
      return this;
    }
    
    public Builder setMaxContentLength(int length)
    {
      instance.maxContentLength = length;
      return this;
    }
    
    public Builder compress(boolean on)
    {
      instance.compress = on;
      return this;
    }
    
    public Builder setMetricsCollector(MetricsCollector recorder)
    {
      instance.metricsCollector = recorder;
      return this;
    }
    
    public ChannelConfiguration build()
    {
      assertValidState();
      var res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState()
    {
      if (instance.maxContentLength <= 0) {
        instance.maxContentLength = 1048576;
      }
      if (instance.metricsCollector == null) {
        throw new IllegalStateException("Missing metrics collector");
      }
      if (instance.inboundHandler == null) {
        throw new IllegalStateException("Missing inbound handler");
      }
      if (instance.readTimeout == null) {
        throw new IllegalStateException("Missing read timeout duration");
      }
      if (instance.writeTimeout == null) {
        throw new IllegalStateException("Missing write timeout duration");
      }
    }
  }
}
