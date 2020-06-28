package com.appsflyer.rta.httpserver;

import com.appsflyer.rta.httpserver.handler.RequestHandler;
import com.appsflyer.rta.httpserver.metrics.MetricsCollector;
import com.appsflyer.rta.httpserver.metrics.MetricsCollectorFactory;

import java.time.Duration;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public final class ServerConfiguration
{
  private int port;
  private EventLoopConfiguration bossGroupConfig;
  private EventLoopConfiguration childGroupConfig;
  private RequestHandler requestHandler;
  private MetricsCollector metricsCollector;
  private String host = "localhost";
  private boolean compress;
  private Duration connectTimeout = Duration.ofSeconds(30L);
  private Duration readTimeout = Duration.ofSeconds(10L);
  private Duration writeTimeout = Duration.ofSeconds(10L);
  
  private ServerConfiguration()
  {
  }
  
  public int port()
  {
    return port;
  }
  
  public String host()
  {
    return host;
  }
  
  public boolean isCompress()
  {
    return compress;
  }
  
  public EventLoopConfiguration bossGroupConfig()
  {
    return bossGroupConfig;
  }
  
  public EventLoopConfiguration childGroupConfig()
  {
    return childGroupConfig;
  }
  
  public Duration connectTimeout()
  {
    return connectTimeout;
  }
  
  public Duration readTimeout()
  {
    return readTimeout;
  }
  
  public Duration writeTimeout()
  {
    return writeTimeout;
  }
  
  public MetricsCollector metricsCollector()
  {
    return metricsCollector;
  }
  
  public RequestHandler requestHandler()
  {
    return requestHandler;
  }
  
  
  public static final class Builder
  {
    private ServerConfiguration instance;
    
    public Builder()
    {
      this(new ServerConfiguration());
    }
    
    private Builder(ServerConfiguration instance) {this.instance = instance;}
    
    public Builder setPort(int port)
    {
      instance.port = port;
      return this;
    }
    
    public Builder setHost(String host)
    {
      Objects.requireNonNull(host);
      instance.host = host;
      return this;
    }
    
    public Builder setCompress(boolean compress)
    {
      instance.compress = compress;
      return this;
    }
    
    public Builder setBossGroupConfig(EventLoopConfiguration bossGroupConfig)
    {
      Objects.requireNonNull(bossGroupConfig);
      instance.bossGroupConfig = bossGroupConfig;
      return this;
    }
    
    public Builder setChildGroupConfig(EventLoopConfiguration childGroupConfig)
    {
      Objects.requireNonNull(childGroupConfig);
      instance.childGroupConfig = childGroupConfig;
      return this;
    }
    
    public Builder setConnectTimeout(Duration connectTimeout)
    {
      Objects.requireNonNull(connectTimeout);
      instance.connectTimeout = connectTimeout;
      return this;
    }
    
    public Builder setReadTimeout(Duration readTimeout)
    {
      Objects.requireNonNull(readTimeout);
      instance.readTimeout = readTimeout;
      return this;
    }
    
    public Builder setWriteTimeout(Duration writeTimeout)
    {
      Objects.requireNonNull(writeTimeout);
      instance.writeTimeout = writeTimeout;
      return this;
    }
    
    public Builder setRequestHandler(RequestHandler requestHandler)
    {
      Objects.requireNonNull(requestHandler);
      instance.requestHandler = requestHandler;
      return this;
    }
    
    public Builder setMetricsCollector(MetricsCollector metricsCollector)
    {
      instance.metricsCollector = metricsCollector;
      return this;
    }
    
    public ServerConfiguration build()
    {
      assertValidState();
      ServerConfiguration res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState()
    {
      if (instance.bossGroupConfig == null) {
        throw new IllegalStateException("Missing boss event loop group configuration");
      }
      if (instance.childGroupConfig == null) {
        throw new IllegalStateException("Missing child event loop group configuration");
      }
      if (instance.requestHandler == null) {
        throw new IllegalStateException("Missing request handler");
      }
      if (instance.port <= 0 || instance.port > 65535) {
        throw new IllegalStateException("Invalid HTTP port ");
      }
      if (instance.metricsCollector == null) {
        instance.metricsCollector = MetricsCollectorFactory.NOOP;
      }
    }
  }
}
