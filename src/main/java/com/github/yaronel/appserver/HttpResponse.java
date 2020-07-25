package com.github.yaronel.appserver;

import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Recycler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public final class HttpResponse implements Recyclable
{
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
  private final Recycler.Handle<HttpResponse> handle;
  
  private int statusCode;
  private byte[] content;
  private Map<String, String> headers;
  private HttpVersion protocol;
  
  private static final Recycler<HttpResponse> RECYCLER = new Recycler<>()
  {
    protected HttpResponse newObject(Recycler.Handle<HttpResponse> handle)
    {
      return new HttpResponse(handle);
    }
  };
  
  private HttpResponse(Recycler.Handle<HttpResponse> handle)
  {
    this.handle = handle;
  }
  
  public static HttpResponse newInstance(int statusCode,
                                         byte[] content,
                                         Map<String, String> headers,
                                         HttpVersion protocol)
  {
    if (statusCode < 100 || statusCode > 599) {
      throw new IllegalStateException(
          String.format("Invalid status code: %d", statusCode));
    }
    
    HttpResponse instance = RECYCLER.get();
    instance.statusCode = statusCode;
    instance.content = Objects.requireNonNullElse(content, EMPTY_BYTE_ARRAY);
    instance.headers = Objects.requireNonNullElse(headers, EMPTY_MAP);
    instance.protocol = Objects.requireNonNullElse(protocol, HTTP_1_1);
    return instance;
  }
  
  public static HttpResponse newInstance(int statusCode,
                                         byte[] content,
                                         Map<String, String> headers,
                                         String protocol)
  {
    return newInstance(statusCode, content, headers, HttpVersion.valueOf(protocol));
  }
  
  public static HttpResponse newInstance(int statusCode, byte[] content, Map<String, String> headers)
  {
    return newInstance(statusCode, content, headers, HTTP_1_1);
  }
  
  public static HttpResponse newInstance(int statusCode, byte[] content)
  {
    return newInstance(statusCode, content, EMPTY_MAP);
  }
  
  public static HttpResponse newInstance(int statusCode)
  {
    return newInstance(statusCode, EMPTY_BYTE_ARRAY);
  }
  
  public static HttpResponse newInstance()
  {
    return newInstance(200);
  }
  
  public int statusCode()
  {
    return statusCode;
  }
  
  /**
   * @return The body of the response as a byte array. Returns the underlining
   * array and therefor any changes will mutate the response.
   */
  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public byte[] content()
  {
    return content;
  }
  
  /**
   * @return The response headers as a String -> String map Returns the underlining
   * Map instance and therefor any changes will mutate the response.
   */
  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public Map<String, String> headers()
  {
    return headers;
  }
  
  public HttpVersion protocol()
  {
    return protocol;
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    
    HttpResponse that = (HttpResponse) o;
    
    if (statusCode != that.statusCode) { return false; }
    if (!Arrays.equals(content, that.content)) { return false; }
    if (!Objects.equals(headers, that.headers)) {
      return false;
    }
    return protocol.equals(that.protocol);
  }
  
  @Override
  public int hashCode()
  {
    int result = statusCode;
    result = 31 * result + Arrays.hashCode(content);
    result = 31 * result + (headers != null ? headers.hashCode() : 0);
    result = 31 * result + protocol.hashCode();
    return result;
  }
  
  @Override
  public boolean recycle()
  {
    statusCode = 0;
    content = null;
    headers = null;
    protocol = null;
    handle.recycle(this);
    return true;
  }
}
