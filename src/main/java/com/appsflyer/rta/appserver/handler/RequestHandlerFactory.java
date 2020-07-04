package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.ServerConfig;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class RequestHandlerFactory
{
  private RequestHandlerFactory() {}
  
  public static ChannelInboundHandlerAdapter newInstance(ServerConfig config)
  {
    switch (config.mode()) {
      case BLOCKING:
      case NON_BLOCKING:
        return new SyncRequestHandler(config.requestHandler(), config.metricsCollector());
      case ASYNC:
        return new AsyncRequestHandler(config.requestHandler(), config.metricsCollector());
    }
    throw new UnsupportedOperationException(String.format("Unsupported mode %s", config.mode()));
  }
}
