package com.appsflyer.af_netty;

import com.appsflyer.af_netty.util.MetricsCollector;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@ChannelHandler.Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
  private final Function<HttpRequest, HttpResponse> handlerImpl;
  private final MetricsCollector metricsCollector;
  
  public HttpServerHandler(Function<HttpRequest, HttpResponse> handlerImpl, MetricsCollector metricsCollector)
  {
    this.handlerImpl = handlerImpl;
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    FullHttpRequest req = (FullHttpRequest) msg;
    HttpResponse inboundResponse = callHandler(HttpRequest.newInstance(req, ctx.channel()));
    FullHttpResponse outboundResponse =
        new DefaultFullHttpResponse(
            req.protocolVersion(),
            HttpResponseStatus.valueOf(inboundResponse.statusCode()),
            PooledByteBufAllocator.DEFAULT
                .directBuffer(inboundResponse.content().length)
                .writeBytes(inboundResponse.content()),
            false);
    
    setHeaders(inboundResponse, outboundResponse);
    
    req.release();
    ctx.write(outboundResponse, ctx.voidPromise());
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
    HttpResponse response = handlerImpl.apply(request);
    metricsCollector.recordServiceLatency(Duration.ofNanos(System.nanoTime() - startTime));
    request.recycle();
    return response;
  }
  
  /**
   * Sets {@code inboundResponse}'s headers on {@code outboundResponse}.
   *
   * @param inboundResponse  The response created by the user
   * @param outboundResponse The response that will be written back to the client
   */
  private void setHeaders(HttpResponse inboundResponse, FullHttpResponse outboundResponse)
  {
    HttpHeaders outHeaders = outboundResponse.headers();
    for (Map.Entry<String, String> entry : inboundResponse.headers().entrySet()) {
      outHeaders.set(entry.getKey(), entry.getValue());
    }
    outHeaders.set(CONTENT_LENGTH, outboundResponse.content().readableBytes());
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    logger.error(cause.getMessage());
    var suppressed = cause.getSuppressed();
    if (suppressed.length > 0) {
      logger.error("Printing suppressed exceptions:");
      for (int i = 0; i < suppressed.length; i++) {
        logger.error("Suppressed {}/{}: {}", i + 1, suppressed.length, suppressed[i].getMessage());
      }
    }
    ctx.close();
  }
}
