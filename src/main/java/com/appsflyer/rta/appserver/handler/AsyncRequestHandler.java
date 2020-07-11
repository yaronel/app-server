package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@SuppressWarnings("WeakerAccess")
@ChannelHandler.Sharable
public class AsyncRequestHandler extends ChannelInboundHandlerAdapter
{
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
    //noinspection OverlyLongLambda
    requestHandler
        .applyAsync(request)
        .handle((response, throwable) -> {
          metricsCollector.recordServiceLatency(System.nanoTime() - startTime);
          if (throwable == null) {
            ctx.write(response, ctx.voidPromise());
          }
          else {
            exceptionCaught(ctx, throwable);
          }
          request.recycle();
          //noinspection ReturnOfNull
          return null;
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
    HandlerUtil.logException(cause);
    ctx.write(HandlerUtil.createServerError(), ctx.voidPromise());
  }
}
