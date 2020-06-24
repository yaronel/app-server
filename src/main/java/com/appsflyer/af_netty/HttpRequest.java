package com.appsflyer.af_netty;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpRequest
{
  private final FullHttpRequest impl;
  private final QueryStringDecoder queryDecoder;
  private final Headers headers;
  private final Channel channel;
  
  public HttpRequest(FullHttpRequest impl, Channel channel)
  {
    this.impl = impl;
    this.channel = channel;
    queryDecoder = new QueryStringDecoder(impl.uri());
    headers = new Headers(impl.headers());
  }
  
  public Headers headers()
  {
    return headers;
  }
  
  public String scheme()
  {
    // @todo - Hack to determine the scheme. Keep looking for a better way.
    return channel.pipeline().get(SslHandler.class) == null ? "http" : "https";
  }
  
  public String path()
  {
    return queryDecoder.path();
  }
  
  public String rawQuery()
  {
    return queryDecoder.rawQuery();
  }
  
  public Map<String, List<String>> queryParameters()
  {
    return queryDecoder.parameters();
  }
  
  public String method()
  {
    return impl.method().name();
  }
  
  public String protocol()
  {
    return impl.protocolVersion().text();
  }
  
  public String asString()
  {
    return impl.content().toString(StandardCharsets.UTF_8);
  }
  
  public byte[] asBytes()
  {
    return ByteBufUtil.getBytes(impl.content());
  }
  
  public InputStream asStream()
  {
    return new ByteBufInputStream(impl.content(), true);
  }
  
  public String serverName()
  {
    return ((InetSocketAddress) channel.localAddress()).getHostString();
  }
  
  public int serverPort()
  {
    return ((InetSocketAddress) channel.localAddress()).getPort();
  }
}
