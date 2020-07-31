package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpRequest;
import com.github.yaronel.appserver.HttpResponse;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.SystemClock;
import com.github.yaronel.appserver.metrics.TimeProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

@SuppressWarnings("WeakerAccess")
public class SyncRequestHandler extends ChannelInboundHandlerAdapter
{
  private final RequestHandler<HttpRequest, HttpResponse> requestHandler;
  private final MetricsCollector metricsCollector;
  private final TimeProvider clock;
  
  public SyncRequestHandler(
      RequestHandler<HttpRequest, HttpResponse> requestHandler,
      MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
    this.clock = new SystemClock();
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    //@todo look in to calling recordServiceLatency once.
    HttpRequest request = (HttpRequest) msg;
    long start = clock.time();
    try {
      HttpResponse response = requestHandler.apply(request);
      metricsCollector.recordServiceLatency(clock.time() - start);
      ctx.write(response, ctx.voidPromise());
    } catch (RuntimeException ex) {
      metricsCollector.recordServiceLatency(clock.time() - start);
      exceptionCaught(ctx, ex);
    } finally {
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
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    HandlerUtil.logException(cause);
    ctx.write(HandlerUtil.createServerError());
  }
  
  
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
  {
    if (evt instanceof IdleStateEvent) {
      ctx.close();
    }
  }
}
