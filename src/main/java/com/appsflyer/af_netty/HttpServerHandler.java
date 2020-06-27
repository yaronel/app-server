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

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@ChannelHandler.Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
  private final HttpRequestHandler requestHandler;
  private final MetricsCollector metricsCollector;
  
  public HttpServerHandler(HttpRequestHandler requestHandler, MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    FullHttpRequest req = (FullHttpRequest) msg;
    HttpResponse inboundResponse = callHandler(HttpRequest.newInstance(req, ctx.channel()));
    ctx.write(newFullHttpResponse(inboundResponse), ctx.voidPromise());
    
    inboundResponse.recycle();
    req.release();
  }
  
  private FullHttpResponse newFullHttpResponse(HttpResponse response)
  {
    var content = response.content();
    return setHeaders(
        new DefaultFullHttpResponse(response.protocol(),
                                    HttpResponseStatus.valueOf(response.statusCode()),
                                    PooledByteBufAllocator.DEFAULT
                                        .directBuffer(content.length)
                                        .writeBytes(content),
                                    false),
        response.headers());
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
    request.recycle();
    return response;
  }
  
  /**
   * Sets headers of the {@code response}.
   *
   * @param response The response that will be written back to the client
   * @param headers  The response created by the user
   */
  private FullHttpResponse setHeaders(FullHttpResponse response, Map<String, String> headers)
  {
    HttpHeaders outHeaders = response.headers();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      outHeaders.set(entry.getKey(), entry.getValue());
    }
    outHeaders.set(CONTENT_LENGTH, response.content().readableBytes());
    return response;
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
