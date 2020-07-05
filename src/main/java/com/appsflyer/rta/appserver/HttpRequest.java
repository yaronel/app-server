package com.appsflyer.rta.appserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Recycler;
import io.netty.util.ReferenceCountUtil;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    instance.queryDecoder = new QueryStringDecoder(QueryStringDecoder.decodeComponent(impl.uri(), UTF_8));
    instance.headers = Headers.newInstance(impl.headers());
    if (isFormData(impl)) {
      instance.formData = QueryStringDecoder.decodeComponent(impl.content().toString(UTF_8));
    }
  
    return instance;
  }
  
  private static boolean isFormData(FullHttpRequest impl)
  {
    return impl
        .headers()
        .get(HttpHeaderNames.CONTENT_TYPE, "")
        .contentEquals(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
  }
  
  private final Recycler.Handle<HttpRequest> handle;
  private FullHttpRequest impl;
  private QueryStringDecoder queryDecoder;
  private Headers headers;
  private Channel channel;
  private String formData;
  
  private HttpRequest(Recycler.Handle<HttpRequest> handle)
  {
    this.handle = handle;
  }
  
  public Map<String, String> headers()
  {
    return headers.getAll();
  }
  
  public String scheme()
  {
    // @todo - Hack to determine the scheme. Keep looking for a better way.
    return channel.pipeline().get(SslHandler.class) == null ? "http" : "https";
  }
  
  public String path()
  {
    return queryDecoder.rawPath();
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
    if (formData != null) {
      return formData;
    }
    return impl.content().toString(UTF_8);
  }
  
  public byte[] asBytes()
  {
    if (formData != null) {
      return formData.getBytes(UTF_8);
    }
    return ByteBufUtil.getBytes(impl.content());
  }
  
  public InputStream asStream()
  {
    ByteBuf content;
    if (formData != null) {
      content = Unpooled.wrappedBuffer(formData.getBytes(UTF_8));
    }
    else {
      content = impl.content();
    }
    return new ByteBufInputStream(content, true);
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
    ReferenceCountUtil.release(impl);
    impl = null;
    channel = null;
    queryDecoder = null;
    headers = null;
    formData = null;
    handle.recycle(this);
    return true;
  }
}
