package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import com.appsflyer.rta.appserver.util.HandlerUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@ChannelHandler.Sharable
public class AsyncRequestHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(AsyncRequestHandler.class);
  private final RequestHandler requestHandler;
  private final MetricsCollector metricsCollector;
  
  public AsyncRequestHandler(RequestHandler requestHandler, MetricsCollector metricsCollector)
  {
    
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    HttpRequest request = (HttpRequest) msg;
    var startTime = System.nanoTime();
    CompletableFuture<HttpResponse> futureResponse = requestHandler.applyAsync(request);
    //noinspection OverlyLongLambda
    futureResponse.handle((httpResponse, throwable) -> {
      metricsCollector.recordServiceLatency(Duration.ofNanos(System.nanoTime() - startTime));
      if (throwable == null) {
        ctx.write(httpResponse, ctx.voidPromise());
      }
      else {
        exceptionCaught(ctx, throwable);
      }
      request.recycle();
      return httpResponse;
    });
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
    HandlerUtil.logException(logger, cause);
  }
}
