package com.appsflyer.af_netty.channel;

import com.appsflyer.af_netty.metrics.MetricsCollector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.Future;

import java.time.Duration;

public class HttpServerMetricsHandler extends ChannelDuplexHandler
{
  private long bytesReceived;
  private long bytesSent;
  private long receiveLatency;
  private long sendLatency;
  private final MetricsCollector metricsCollector;
  
  HttpServerMetricsHandler(MetricsCollector metricsCollector)
  {
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
  {
    if (msg instanceof HttpResponse) {
      if (((HttpResponse) msg).status().equals(HttpResponseStatus.CONTINUE)) {
        ctx.write(msg, promise);
        return;
      }
      sendLatency = System.nanoTime();
    }
    
    if (msg instanceof ByteBufHolder) {
      bytesSent += ((ByteBufHolder) msg).content().readableBytes();
    }
    else if (msg instanceof ByteBuf) {
      bytesSent += ((ByteBuf) msg).readableBytes();
    }
    
    if (msg instanceof LastHttpContent) {
      promise.addListener(this::sumMetrics);
    }
    
    ctx.write(msg, promise);
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    if (msg instanceof HttpRequest) {
      receiveLatency = System.nanoTime();
    }
    
    if (msg instanceof ByteBufHolder) {
      bytesReceived += ((ByteBufHolder) msg).content().readableBytes();
    }
    else if (msg instanceof ByteBuf) {
      bytesReceived += ((ByteBuf) msg).readableBytes();
    }
    
    if (msg instanceof LastHttpContent) {
      metricsCollector.recordReceiveLatency(Duration.ofNanos(System.nanoTime() - receiveLatency));
      metricsCollector.markBytesReceived(bytesReceived);
      metricsCollector.markHit();
      bytesReceived = 0;
    }
    
    ctx.fireChannelRead(msg);
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    metricsCollector.markErrorHit();
    ctx.fireExceptionCaught(cause);
  }
  
  private void sumMetrics(Future<? super Void> future)
  {
    metricsCollector.recordSendLatency(Duration.ofNanos(System.nanoTime() - sendLatency));
    if (receiveLatency != 0) {
      metricsCollector.recordResponseLatency(Duration.ofNanos(System.nanoTime() - receiveLatency));
    }
    else {
      metricsCollector.recordResponseLatency(Duration.ofNanos(System.nanoTime() - sendLatency));
    }
    metricsCollector.markSentBytes(bytesSent);
    metricsCollector.markSuccessHit();
    bytesSent = 0;
  }
}
