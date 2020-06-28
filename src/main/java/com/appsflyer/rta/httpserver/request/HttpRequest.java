package com.appsflyer.rta.httpserver.request;

import com.appsflyer.rta.httpserver.Recyclable;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Recycler;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class HttpRequest implements Recyclable
{
  private static final Recycler<HttpRequest> RECYCLER = new Recycler<>()
  {
    protected HttpRequest newObject(Recycler.Handle<HttpRequest> handle)
    {
      return new HttpRequest(handle);
    }
  };
  
  public static HttpRequest newInstance(FullHttpRequest impl, Channel channel)
  {
    HttpRequest instance = RECYCLER.get();
    instance.impl = impl;
    instance.channel = channel;
    instance.queryDecoder = new QueryStringDecoder(impl.uri());
    instance.headers = Headers.newInstance(impl.headers());
    return instance;
  }
  
  private final Recycler.Handle<HttpRequest> handle;
  private FullHttpRequest impl;
  private QueryStringDecoder queryDecoder;
  private Headers headers;
  private Channel channel;
  
  private HttpRequest(Recycler.Handle<HttpRequest> handle)
  {
    this.handle = handle;
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
  
  @Override
  public boolean recycle()
  {
    headers.recycle();
    impl.release();
    impl = null;
    channel = null;
    queryDecoder = null;
    headers = null;
    handle.recycle(this);
    return true;
  }
}
