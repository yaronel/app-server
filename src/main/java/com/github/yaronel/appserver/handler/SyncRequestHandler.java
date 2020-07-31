package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpRequest;
import com.github.yaronel.appserver.HttpResponse;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.SystemClock;
import com.github.yaronel.appserver.metrics.TimeProvider;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("WeakerAccess")
public class SyncRequestHandler extends RequestHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class.getName());
  private final UserRequestHandler<HttpRequest, HttpResponse> requestHandler;
  private final MetricsCollector metricsCollector;
  private final TimeProvider clock;
  
  public SyncRequestHandler(
      UserRequestHandler<HttpRequest, HttpResponse> requestHandler,
      MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
    this.clock = new SystemClock();
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    HttpRequest request = (HttpRequest) msg;
    HttpResponse response = null;
    Exception error = null;
    long start = clock.time();
    try {
      response = requestHandler.apply(request);
    } catch (RuntimeException ex) {
      error = ex;
    } finally {
      metricsCollector.recordServiceLatency(clock.time() - start);
      if (error != null) {
        exceptionCaught(ctx, error);
      }
      else {
        ctx.write(response, ctx.voidPromise());
      }
      request.recycle();
    }
  }
  
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx)
  {
    ctx.flush();
    ctx.fireChannelReadComplete();
  }
  
  @Override
  public Logger logger()
  {
    return logger;
  }
}
