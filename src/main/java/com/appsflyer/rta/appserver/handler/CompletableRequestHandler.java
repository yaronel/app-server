package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import com.appsflyer.rta.appserver.metrics.Stopper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("WeakerAccess")
public class CompletableRequestHandler extends ChannelInboundHandlerAdapter
{
  private final RequestHandler<HttpRequest, HttpResponse> requestHandler;
  private final MetricsCollector metricsCollector;
  
  public CompletableRequestHandler(
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
    
    // Slightly long lambda is unavoidable here to maintain references in scope
    //noinspection OverlyLongLambda
    exec(request)
        .handle((response, throwable) -> {
          metricsCollector.recordServiceLatency(timer.stop());
          if (throwable == null) {
            ctx.writeAndFlush(response, ctx.voidPromise());
          }
          else {
            exceptionCaught(ctx, throwable);
          }
          request.recycle();
          timer.recycle();
          //noinspection ReturnOfNull
          return null;
        });
  }
  
  private CompletableFuture<HttpResponse> exec(HttpRequest request)
  {
    try {
      return requestHandler.applyAsync(request);
    } catch (RuntimeException ex) {
      return CompletableFuture.failedFuture(ex);
    }
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    HandlerUtil.logException(cause);
    ctx.writeAndFlush(HandlerUtil.createServerError(), ctx.voidPromise());
  }
  
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
  {
    if (evt instanceof IdleStateEvent) {
      ctx.close();
    }
  }
}
