package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpRequest;
import com.github.yaronel.appserver.HttpResponse;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.Stopper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

@SuppressWarnings("WeakerAccess")
public class SyncRequestHandler extends ChannelInboundHandlerAdapter
{
  private final RequestHandler<HttpRequest, HttpResponse> requestHandler;
  private final MetricsCollector metricsCollector;
  
  public SyncRequestHandler(
      RequestHandler<HttpRequest, HttpResponse> requestHandler,
      MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    HttpRequest request = (HttpRequest) msg;
    Stopper timer = Stopper.newStartedInstance();
    try {
      HttpResponse response = requestHandler.apply(request);
      metricsCollector.recordServiceLatency(timer.stop());
      ctx.write(response, ctx.voidPromise());
    } catch (RuntimeException ex) {
      metricsCollector.recordServiceLatency(timer.stop());
      exceptionCaught(ctx, ex);
    } finally {
      request.recycle();
      timer.recycle();
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
