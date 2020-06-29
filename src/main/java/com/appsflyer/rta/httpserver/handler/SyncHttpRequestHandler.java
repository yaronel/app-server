package com.appsflyer.rta.httpserver.handler;

import com.appsflyer.rta.httpserver.HttpResponse;
import com.appsflyer.rta.httpserver.metrics.MetricsCollector;
import com.appsflyer.rta.httpserver.request.HttpRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@ChannelHandler.Sharable
public class SyncHttpRequestHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(SyncHttpRequestHandler.class);
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
    ctx.write(callHandler(request), ctx.voidPromise());
    request.recycle();
  }
  
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx)
  {
    ctx.flush();
    ctx.fireChannelReadComplete();
  }
  
  private HttpResponse callHandler(HttpRequest request)
  {
    var startTime = System.nanoTime();
    HttpResponse response = requestHandler.apply(request);
    metricsCollector.recordServiceLatency(Duration.ofNanos(System.nanoTime() - startTime));
    return response;
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    logger.error(cause.getMessage());
    Throwable[] suppressed = cause.getSuppressed();
    if (suppressed.length > 0) {
      logger.error("Printing suppressed exceptions:");
      for (int i = 0; i < suppressed.length; i++) {
        //noinspection HardcodedFileSeparator
        logger.error("Suppressed {}/{}: {}", i + 1, suppressed.length, suppressed[i].getMessage());
      }
    }
    ctx.close();
  }
}
