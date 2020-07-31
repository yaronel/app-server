package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.SystemClock;
import com.github.yaronel.appserver.metrics.TimeProvider;
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

public class ServerMetricsHandler extends ChannelDuplexHandler
{
  private final MetricsCollector metricsCollector;
  private final TimeProvider clock;
  private long bytesReceived;
  private long bytesSent;
  private long receiveLatency;
  private long sendLatency;
  
  
  ServerMetricsHandler(MetricsCollector metricsCollector, TimeProvider clock)
  {
    this.metricsCollector = metricsCollector;
    this.clock = clock;
  }
  
  ServerMetricsHandler(MetricsCollector metricsCollector)
  {
    this(metricsCollector, new SystemClock());
  }
  
  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
  {
    if (msg instanceof HttpResponse) {
      if (((HttpResponse) msg).status().equals(HttpResponseStatus.CONTINUE)) {
        ctx.write(msg, promise);
        return;
      }
      sendLatency = clock.time();
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
      receiveLatency = clock.time();
    }
    
    if (msg instanceof ByteBufHolder) {
      bytesReceived += ((ByteBufHolder) msg).content().readableBytes();
    }
    else if (msg instanceof ByteBuf) {
      bytesReceived += ((ByteBuf) msg).readableBytes();
    }
    
    if (msg instanceof LastHttpContent) {
      metricsCollector.recordReceiveLatency(clock.time() - receiveLatency);
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
    ctx.close();
  }
  
  private void sumMetrics(Future<? super Void> future)
  {
    metricsCollector.recordSendLatency(clock.time() - sendLatency);
    if (receiveLatency == 0) {
      metricsCollector.recordResponseLatency(clock.time() - sendLatency);
    }
    else {
      metricsCollector.recordResponseLatency(clock.time() - receiveLatency);
    }
    metricsCollector.markSentBytes(bytesSent);
    metricsCollector.markSuccessHit();
    bytesSent = 0;
  }
}
