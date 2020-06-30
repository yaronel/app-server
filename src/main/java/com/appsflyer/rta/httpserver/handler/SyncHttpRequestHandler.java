package com.appsflyer.rta.httpserver.handler;

import com.appsflyer.rta.httpserver.HttpResponse;
import com.appsflyer.rta.httpserver.metrics.MetricsCollector;
import com.appsflyer.rta.httpserver.request.HttpRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.time.Duration;

@ChannelHandler.Sharable
public final class SyncHttpRequestHandler extends HttpRequestHandler
{
  private final SyncRequestHandler requestHandler;
  private final MetricsCollector metricsCollector;
  
  public SyncHttpRequestHandler(SyncRequestHandler requestHandler, MetricsCollector metricsCollector)
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
      metricsCollector.recordServiceLatency(Duration.ofNanos(System.nanoTime() - startTime));
      ctx.write(response, ctx.voidPromise());
    } catch (RuntimeException ex) {
      metricsCollector.recordServiceLatency(Duration.ofNanos(System.nanoTime() - startTime));
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
}
