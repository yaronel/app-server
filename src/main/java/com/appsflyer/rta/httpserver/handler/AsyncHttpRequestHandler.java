package com.appsflyer.rta.httpserver.handler;

import com.appsflyer.rta.httpserver.metrics.MetricsCollector;
import com.appsflyer.rta.httpserver.request.HttpRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.time.Duration;

@ChannelHandler.Sharable
public final class AsyncHttpRequestHandler extends HttpRequestHandler
{
  private final AsyncRequestHandler requestHandler;
  private final MetricsCollector metricsCollector;
  
  public AsyncHttpRequestHandler(AsyncRequestHandler requestHandler,
                                 MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    HttpRequest request = (HttpRequest) msg;
    var startTime = System.nanoTime();
    requestHandler.apply(request)
                  .thenAccept(response -> {
                    metricsCollector.recordServiceLatency(
                        Duration.ofNanos(System.nanoTime() - startTime));
                    ctx.write(response, ctx.voidPromise());
                  })
                  .exceptionally(ex -> {
                    metricsCollector.recordServiceLatency(
                        Duration.ofNanos(System.nanoTime() - startTime));
                    exceptionCaught(ctx, ex);
                    return null;
                  })
                  .thenRun(request::recycle);
  }
  
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx)
  {
    ctx.flush();
    ctx.fireChannelReadComplete();
  }
}
