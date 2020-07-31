package com.github.yaronel.appserver;

import com.github.yaronel.appserver.executor.EventExecutorConfig;
import com.github.yaronel.appserver.executor.ExecutorConfig;
import com.github.yaronel.appserver.handler.UserRequestHandler;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.MetricsCollectorFactory;

import java.time.Duration;
import java.util.Objects;

import static com.github.yaronel.appserver.ExecutionMode.ASYNC;
import static com.github.yaronel.appserver.ExecutionMode.SYNC;

@SuppressWarnings({"ClassWithTooManyFields",
    "ClassWithOnlyPrivateConstructors", "WeakerAccess"})
public class ServerConfig
{
  
  private int port;
  private ExecutorConfig asyncExecutorsConfig;
  private ExecutorConfig childGroupConfig;
  private UserRequestHandler<HttpRequest, HttpResponse> requestHandler;
  private MetricsCollector metricsCollector;
  private boolean compress;
  private int maxContentLength = 1048576;
  private ExecutionMode mode = SYNC;
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
  
  public boolean isCompress()
  {
    return compress;
  }
  
  public ExecutorConfig asyncExecutorsConfig()
  {
    return asyncExecutorsConfig;
  }
  
  public ExecutorConfig childGroupConfig()
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
  
  public ExecutionMode mode()
  {
    return mode;
  }
  
  public UserRequestHandler<HttpRequest, HttpResponse> requestHandler()
  {
    return requestHandler;
  }
  
  public boolean isAsyncHandler()
  {
    return mode == ASYNC;
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
      instance = new ServerConfig();
    }
    
    public Builder setPort(int port)
    {
      instance.port = port;
      return this;
    }
  
    public Builder setCompress(boolean compress)
    {
      instance.compress = compress;
      return this;
    }
  
    public Builder setAsyncExecutorsConfig(ExecutorConfig executorsConfig)
    {
      Objects.requireNonNull(executorsConfig);
      instance.asyncExecutorsConfig = executorsConfig;
      return this;
    }
  
    public Builder setChildGroupConfig(ExecutorConfig executorsConfig)
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
  
    public Builder setMode(ExecutionMode mode)
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
  
    public Builder setRequestHandler(
        UserRequestHandler<HttpRequest, HttpResponse> requestHandler)
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
      if (instance.isAsyncHandler() && instance.asyncExecutorsConfig == null) {
        instance.asyncExecutorsConfig = EventExecutorConfig.defaultConfig();
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

