package com.appsflyer.rta.appserver;

import com.appsflyer.rta.appserver.codec.FullHtmlRequestDecoder;
import com.appsflyer.rta.appserver.codec.FullHtmlResponseEncoder;
import com.appsflyer.rta.appserver.handler.HttpServerMetricsHandler;
import com.appsflyer.rta.appserver.handler.RequestHandlerFactory;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;

class HttpChannelInitializer extends ChannelInitializer<SocketChannel>
{
  private final ServerConfig config;
  private final ChannelInboundHandlerAdapter inboundHandler;
  
  HttpChannelInitializer(ServerConfig config)
  {
    this.config = config;
    inboundHandler = RequestHandlerFactory.newInstance(config);
  }
  
  private EventExecutorGroup createEventExecutorsGroup()
  {
    EventExecutorsConfig executorsConfig = config.blockingExecutorsConfig();
    return new DefaultEventExecutorGroup(
        executorsConfig.threadCount(),
        new DefaultThreadFactory(executorsConfig.name()));
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
    
    if (config.isBlockingIo()) {
      pipeline.addLast(createEventExecutorsGroup(), "app-handler", inboundHandler);
    }
    else {
      pipeline.addLast("app-handler", inboundHandler);
    }
  }
}
