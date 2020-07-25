package com.github.yaronel.appserver;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpRequestTest
{
  private static final String url = "http://www.example.com/lorem";
  
  @Test
  void decodesUrlAndQueryParameters()
  {
    var query = "name=foo&name=bar&greet=Hello World(!)&bar={\"type\":\"json\"}";
    var encodedQuery = "name%3Dfoo%26name%3Dbar%26greet%3DHello%20World(!)%26bar%3D%7B%22type%22%3A%22json%22%7D";
    
    FullHttpRequest originalRequest = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, url + "?" + encodedQuery);
    
    HttpRequest request = HttpRequest.newInstance(originalRequest, mock(Channel.class));
    
    assertEquals(url, request.path());
    assertEquals("GET", request.method());
    assertEquals(query, request.rawQuery());
    
    Map<String, List<String>> queryParameters = request.queryParameters();
    assertEquals(List.of("foo", "bar"), queryParameters.get("name"));
    assertEquals(List.of("Hello World(!)"), queryParameters.get("greet"));
    assertEquals(List.of("{\"type\":\"json\"}"), queryParameters.get("bar"));
    
    request.recycle();
  }
  
  @Test
  void returnsTheServerNameAndPort()
  {
    FullHttpRequest originalRequest = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, url);
    
    int port = 80;
    String host = "localhost";
    
    Channel channel = mock(Channel.class);
    when(channel.localAddress()).thenReturn(new InetSocketAddress(host, port));
    HttpRequest request = HttpRequest.newInstance(originalRequest, channel);
    assertEquals(port, request.serverPort());
    assertEquals(host, request.serverName());
    
    port = 443;
    host = "10.130.200.1";
    
    when(channel.localAddress()).thenReturn(new InetSocketAddress(host, port));
    request = HttpRequest.newInstance(originalRequest, channel);
    assertEquals(port, request.serverPort());
    assertEquals(host, request.serverName());
  }
  
  @Test
  void returnsTheHttpProtocolVersion()
  {
    FullHttpRequest originalRequest = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, url);
    
    HttpRequest request = HttpRequest.newInstance(originalRequest, mock(Channel.class));
    assertEquals("HTTP/1.1", request.protocol());
    
    originalRequest = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_0, HttpMethod.GET, url);
    
    request = HttpRequest.newInstance(originalRequest, mock(Channel.class));
    assertEquals("HTTP/1.0", request.protocol());
    
    originalRequest = new DefaultFullHttpRequest(
        HttpVersion.valueOf("HTTP/2.0"), HttpMethod.GET, url);
    
    request = HttpRequest.newInstance(originalRequest, mock(Channel.class));
    assertEquals("HTTP/2.0", request.protocol());
  }
}
