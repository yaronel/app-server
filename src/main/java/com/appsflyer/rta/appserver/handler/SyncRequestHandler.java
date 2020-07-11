package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@SuppressWarnings("WeakerAccess")
@ChannelHandler.Sharable
public class SyncRequestHandler extends ChannelInboundHandlerAdapter
{
  private final RequestHandler requestHandler;
  private final MetricsCollector metricsCollector;
  
  public SyncRequestHandler(RequestHandler requestHandler, MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    HttpRequest request = (HttpRequest) msg;
    var startTime = System.nanoTime();
    try {
      HttpResponse response = requestHandler.apply(request);
      metricsCollector.recordServiceLatency(System.nanoTime() - startTime);
      ctx.write(response, ctx.voidPromise());
    } catch (RuntimeException ex) {
      metricsCollector.recordServiceLatency(System.nanoTime() - startTime);
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
}
