package com.appsflyer.rta.appserver;

import com.appsflyer.rta.appserver.handler.RequestHandler;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import com.appsflyer.rta.appserver.metrics.MetricsCollectorFactory;

import java.time.Duration;
import java.util.Objects;

import static com.appsflyer.rta.appserver.HandlerMode.BLOCKING;

@SuppressWarnings({"ClassWithTooManyFields",
    "ClassWithOnlyPrivateConstructors", "WeakerAccess"})
public class ServerConfig
{
  
  private int port;
  private EventExecutorsConfig blockingExecutorsConfig;
  private EventExecutorsConfig childGroupConfig;
  private RequestHandler requestHandler;
  private MetricsCollector metricsCollector;
  private boolean compress;
  private int maxContentLength = 1048576;
  private String host = "0.0.0.0";
  private HandlerMode mode = BLOCKING;
  private Duration connectTimeout = Duration.ofSeconds(30L);
  private Duration readTimeout = Duration.ofSeconds(10L);
  private Duration writeTimeout = Duration.ofSeconds(10L);
  
  private ServerConfig()
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
  
  public EventExecutorsConfig blockingExecutorsConfig()
  {
    return blockingExecutorsConfig;
  }
  
  public EventExecutorsConfig childGroupConfig()
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
  
  public HandlerMode mode()
  {
    return mode;
  }
  
  public RequestHandler requestHandler()
  {
    return requestHandler;
  }
  
  public boolean isBlockingIo()
  {
    return mode == BLOCKING;
  }
  
  public int maxContentLength()
  {
    return maxContentLength;
  }
  
  
  public static final class Builder
  {
    private ServerConfig instance;
    
    public Builder()
    {
      this(new ServerConfig());
    }
    
    private Builder(ServerConfig instance) {this.instance = instance;}
    
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
    
    public Builder setBlockingExecutorsConfig(EventExecutorsConfig executorsConfig)
    {
      Objects.requireNonNull(executorsConfig);
      instance.blockingExecutorsConfig = executorsConfig;
      return this;
    }
    
    public Builder setChildGroupConfig(EventExecutorsConfig executorsConfig)
    {
      Objects.requireNonNull(executorsConfig);
      instance.childGroupConfig = executorsConfig;
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
    
    public Builder setMode(HandlerMode mode)
    {
      Objects.requireNonNull(mode);
      instance.mode = mode;
      return this;
    }
    
    public Builder setMaxContentLength(int length)
    {
      if (length <= 0) {
        throw new IllegalArgumentException("Max content length must be greater than 0");
      }
      instance.maxContentLength = length;
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
    
    public ServerConfig build()
    {
      assertValidState();
      ServerConfig res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState()
    {
      if (instance.isBlockingIo() && instance.blockingExecutorsConfig == null) {
        instance.blockingExecutorsConfig = ExecutorsGroupConfig.defaultConfig();
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

