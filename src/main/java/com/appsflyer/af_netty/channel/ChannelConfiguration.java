package com.appsflyer.af_netty.channel;

import io.netty.channel.ChannelInboundHandler;

import java.time.Duration;
import java.util.Objects;

public class ChannelConfiguration
{
  private int maxContentLength;
  private Duration readTimeout;
  private Duration writeTimeout;
  private ChannelInboundHandler inboundHandler;
  private boolean compress;
  
  public static Builder newBuilder()
  {
    return new Builder(new ChannelConfiguration());
  }
  
  private ChannelConfiguration() {}
  
  public int maxContentLength()
  {
    return maxContentLength;
  }
  
  public Duration readTimeout()
  {
    return readTimeout;
  }
  
  public Duration writeTimeout()
  {
    return writeTimeout;
  }
  
  public ChannelInboundHandler inboundHandler()
  {
    return inboundHandler;
  }
  
  public boolean isCompress()
  {
    return compress;
  }
  
  public static class Builder
  {
    private ChannelConfiguration instance;
    
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
