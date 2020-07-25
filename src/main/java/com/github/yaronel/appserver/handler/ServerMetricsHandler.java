package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.Recyclable;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.Stopper;
import com.github.yaronel.appserver.metrics.Timer;
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
  private final Timer receiveLatency;
  private final Timer sendLatency;
  private long bytesReceived;
  private long bytesSent;
  
  
  ServerMetricsHandler(MetricsCollector metricsCollector, Timer receiveLatency, Timer sendLatency)
  {
    this.metricsCollector = metricsCollector;
    this.receiveLatency = receiveLatency;
    this.sendLatency = sendLatency;
  }
  
  ServerMetricsHandler(MetricsCollector metricsCollector)
  {
    this(metricsCollector, Stopper.newInstance(), Stopper.newInstance());
  }
  
  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
  {
    if (msg instanceof HttpResponse) {
      if (((HttpResponse) msg).status().equals(HttpResponseStatus.CONTINUE)) {
        ctx.write(msg, promise);
        return;
      }
      sendLatency.start();
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
  
  private void sumMetrics(Future<? super Void> future)
  {
    metricsCollector.recordSendLatency(sendLatency.stop());
    if (receiveLatency.elapsed() == 0) {
      metricsCollector.recordResponseLatency(sendLatency.elapsed());
    }
    else {
      metricsCollector.recordResponseLatency(receiveLatency.stop());
    }
    metricsCollector.markSentBytes(bytesSent);
    metricsCollector.markSuccessHit();
    bytesSent = 0;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    if (msg instanceof HttpRequest) {
      receiveLatency.start();
    }
    
    if (msg instanceof ByteBufHolder) {
      bytesReceived += ((ByteBufHolder) msg).content().readableBytes();
    }
    else if (msg instanceof ByteBuf) {
      bytesReceived += ((ByteBuf) msg).readableBytes();
    }
    
    if (msg instanceof LastHttpContent) {
      metricsCollector.recordReceiveLatency(receiveLatency.stop());
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
  
  @Override
  public void handlerRemoved(ChannelHandlerContext ctx)
  {
    ((Recyclable) receiveLatency).recycle();
    ((Recyclable) sendLatency).recycle();
  }
}
