package com.github.yaronel.appserver.codec;

import com.github.yaronel.appserver.HttpRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

@ChannelHandler.Sharable
public class FullHtmlRequestDecoder extends MessageToMessageDecoder<FullHttpRequest>
{
  public static final ChannelHandler INSTANCE = new FullHtmlRequestDecoder();
  
  @Override
  protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out)
  {
    out.add(HttpRequest.newInstance(msg, ctx.channel()));
    msg.retain();
  }
}
