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
  static final String SERVER_CODEC = "codec";
  static final String METRICS_HANDLER = "metrics";
  static final String DECOMPRESSOR = "inflate";
  static final String COMPRESSOR = "deflate";
  static final String KEEP_ALIVE_HANDLER = "keep-alive";
  static final String AGGREGATOR_HANDLER = "aggregator";
  static final String WRITE_TIMEOUT_HANDLER = "write-timeout";
  static final String REQUEST_DECODER = "full-html-request-decoder";
  static final String RESPONSE_ENCODER = "full-html-response-encoder";
  static final String APP_HANDLER = "ap-handler";
  
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
    pipeline.addLast(SERVER_CODEC, new HttpServerCodec())
            .addLast(METRICS_HANDLER, new HttpServerMetricsHandler(config.metricsCollector()));
  
    if (config.isCompress()) {
      pipeline.addLast(DECOMPRESSOR, new HttpContentDecompressor())
              .addLast(COMPRESSOR, new HttpContentCompressor());
    }
  
    pipeline.addLast(KEEP_ALIVE_HANDLER, new HttpServerKeepAliveHandler())
            .addLast(AGGREGATOR_HANDLER, new HttpObjectAggregator(config.maxContentLength()))
            .addLast(WRITE_TIMEOUT_HANDLER, new WriteTimeoutHandler(
                (int) config.writeTimeout().getSeconds()))
            .addLast(REQUEST_DECODER, FullHtmlRequestDecoder.INSTANCE)
            .addLast(RESPONSE_ENCODER, FullHtmlResponseEncoder.INSTANCE);
  
    if (config.isBlockingIo()) {
      pipeline.addLast(createEventExecutorsGroup(), APP_HANDLER, inboundHandler);
    }
    else {
      pipeline.addLast(APP_HANDLER, inboundHandler);
    }
  }
}
