package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.ServerConfig;
import com.appsflyer.rta.appserver.codec.FullHtmlRequestDecoder;
import com.appsflyer.rta.appserver.codec.FullHtmlResponseEncoder;
import com.appsflyer.rta.appserver.executor.DefaultExecutor;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.EventExecutorGroup;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel>
{
  static final String IDLE_STATE_HANDLER = "idle";
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
  private EventExecutorGroup asyncExecutors;
  
  public HttpChannelInitializer(ServerConfig config)
  {
    this.config = config;
    if (config.isAsyncHandler()) {
      asyncExecutors =
          DefaultExecutor.newEventExecutorGroup(config.asyncExecutorsConfig());
    }
  }
  
  @Override
  protected void initChannel(SocketChannel ch)
  {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(IDLE_STATE_HANDLER, new IdleStateHandler(0, 0, 60))
            .addLast(SERVER_CODEC, new HttpServerCodec())
            .addLast(METRICS_HANDLER, new ServerMetricsHandler(config.metricsCollector()))
            .addLast(DECOMPRESSOR, new HttpContentDecompressor());
  
    if (config.isCompress()) {
      pipeline.addLast(COMPRESSOR, new HttpContentCompressor());
    }
  
    pipeline.addLast(KEEP_ALIVE_HANDLER, new HttpServerKeepAliveHandler())
            .addLast(AGGREGATOR_HANDLER, new HttpObjectAggregator(config.maxContentLength()))
            .addLast(WRITE_TIMEOUT_HANDLER, new WriteTimeoutHandler(
                (int) config.writeTimeout().getSeconds()))
            .addLast(REQUEST_DECODER, FullHtmlRequestDecoder.INSTANCE)
            .addLast(RESPONSE_ENCODER, FullHtmlResponseEncoder.INSTANCE);
  
    if (asyncExecutors != null) {
      pipeline.addLast(asyncExecutors, APP_HANDLER, RequestHandlerFactory.newInstance(config));
    }
    else {
      pipeline.addLast(APP_HANDLER, RequestHandlerFactory.newInstance(config));
    }
  }
  
  public void shutdownExecutors()
  {
    if (asyncExecutors != null) {
      try {
        asyncExecutors.shutdownGracefully().sync();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
