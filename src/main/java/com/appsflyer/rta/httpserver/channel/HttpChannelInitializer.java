package com.appsflyer.rta.httpserver.channel;

import com.appsflyer.rta.httpserver.handler.FullHtmlRequestDecoder;
import com.appsflyer.rta.httpserver.handler.FullHtmlResponseEncoder;
import com.appsflyer.rta.httpserver.handler.HttpServerMetricsHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.WriteTimeoutHandler;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel>
{
  private final ChannelConfiguration config;
  
  public HttpChannelInitializer(ChannelConfiguration config)
  {
    this.config = config;
  }
  
  @Override
  protected void initChannel(SocketChannel ch)
  {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast("codec", new HttpServerCodec())
            .addLast("metrics", new HttpServerMetricsHandler(config.metricsCollector()));
    
    if (config.isCompress()) {
      pipeline.addLast("inflate", new HttpContentDecompressor())
              .addLast("deflate", new HttpContentCompressor());
    }
    
    pipeline.addLast("keep-alive", new HttpServerKeepAliveHandler())
            .addLast("aggregator", new HttpObjectAggregator(config.maxContentLength()))
            .addLast("write-timeout", new WriteTimeoutHandler(
                (int) config.writeTimeout().getSeconds()))
            .addLast("full-html-request-decoder", FullHtmlRequestDecoder.INSTANCE)
            .addLast("full-html-response-encoder", FullHtmlResponseEncoder.INSTANCE);
    
    if (config.hasEventExecutors()) {
      pipeline.addLast(config.eventExecutors(), "app-handler", config.inboundHandler());
    }
    else {
      pipeline.addLast("app-handler", config.inboundHandler());
    }
  }
}
