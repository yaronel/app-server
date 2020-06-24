package com.appsflyer.af_netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@ChannelHandler.Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
  private final Function<HttpRequest, HttpResponse> handlerImpl;
  
  public HttpServerHandler(Function<HttpRequest, HttpResponse> handlerImpl)
  {
    this.handlerImpl = handlerImpl;
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    FullHttpRequest req = (FullHttpRequest) msg;
    HttpResponse inResponse = handlerImpl.apply(new HttpRequest(req, ctx.channel()));
    
    FullHttpResponse outResponse =
        new DefaultFullHttpResponse(
            req.protocolVersion(),
            HttpResponseStatus.valueOf(inResponse.statusCode()),
            Unpooled.wrappedBuffer(inResponse.content()),
            false);
    
    setHeaders(inResponse, outResponse);
  
    ctx.write(outResponse, ctx.voidPromise());
    req.release();
  }
  
  private void setHeaders(HttpResponse inResponse, FullHttpResponse outResponse)
  {
    HttpHeaders headers = outResponse.headers();
    inResponse.headers().forEach(headers::set);
    headers.set(CONTENT_LENGTH, outResponse.content().readableBytes());
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx)
  {
    ctx.flush();
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    logger.error(cause.getMessage());
    ctx.close();
  }
}
