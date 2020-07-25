package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.ServerConfig;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class RequestHandlerFactory
{
  private RequestHandlerFactory() {}
  
  public static ChannelInboundHandlerAdapter newInstance(ServerConfig config)
  {
    switch (config.mode()) {
      case SYNC:
        return new SyncRequestHandler(config.requestHandler(), config.metricsCollector());
      case ASYNC:
        return new AsyncRequestHandler(config.requestHandler(), config.metricsCollector());
      case NON_BLOCKING:
        return new CompletableRequestHandler(config.requestHandler(), config.metricsCollector());
    }
    throw new UnsupportedOperationException(String.format("Unsupported mode %s", config.mode()));
  }
}
