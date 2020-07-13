package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import com.appsflyer.rta.appserver.metrics.Stopper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

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
    Stopper timer = Stopper.newStartedInstance();
    //noinspection OverlyLongLambda
    requestHandler
        .applyAsync(request)
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
