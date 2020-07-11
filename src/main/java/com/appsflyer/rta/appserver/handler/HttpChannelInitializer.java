package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.ServerConfig;
import com.appsflyer.rta.appserver.codec.FullHtmlRequestDecoder;
import com.appsflyer.rta.appserver.codec.FullHtmlResponseEncoder;
import com.appsflyer.rta.appserver.executor.DefaultExecutor;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.EventExecutorGroup;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel>
{
  static final String SERVER_CODEC = "codec";
  static final String METRICS_HANDLER = "metrics";
  static final String DECOMPRESSOR = "inflate";
  static final String COMPRESSOR = "deflate";
  static final String KEEP_ALIVE_HANDLER = "keep-alive";
  static final String AGGREGATOR_HANDLER = "aggregator";
  static final String WRITE_TIMEOUT_HANDLER = "write-timeout";
  static final String REQUEST_DECODER = "full-html-request-decoder";
  static final String RESPONSE_ENCODER = "full-html-response-encoder";
  static final String APP_HANDLER = "app-handler";
  
  private final ServerConfig config;
  private final ChannelInboundHandlerAdapter inboundHandler;
  private EventExecutorGroup eventExecutors;
  
  public HttpChannelInitializer(ServerConfig config)
  {
    this.config = config;
    inboundHandler = RequestHandlerFactory.newInstance(config);
    if (config.isBlockingIo()) {
      eventExecutors =
          DefaultExecutor.newEventExecutorGroup(config.blockingExecutorsConfig());
    }
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
    
    if (eventExecutors != null) {
      pipeline.addLast(eventExecutors, APP_HANDLER, inboundHandler);
    }
    else {
      pipeline.addLast(APP_HANDLER, inboundHandler);
    }
  }
}
