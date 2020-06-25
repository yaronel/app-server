package com.appsflyer.af_netty.channel;

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
  protected void initChannel(SocketChannel channel)
  {
    ChannelPipeline pipeline = channel.pipeline();
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
            .addLast("handler", config.inboundHandler());
  }
}
