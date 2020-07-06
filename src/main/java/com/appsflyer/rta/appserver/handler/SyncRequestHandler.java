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

@SuppressWarnings("WeakerAccess")
@ChannelHandler.Sharable
public class SyncRequestHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(SyncRequestHandler.class);
  
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
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    HandlerUtil.logException(logger, cause);
    ctx.write(HandlerUtil.createServerError());
  }
}
