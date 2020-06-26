package com.appsflyer.af_netty;

import io.netty.util.Recycler;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class HttpResponse implements Recyclable
{
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
  private final Recycler.Handle<HttpResponse> handle;
  
  private int statusCode;
  private byte[] content;
  private Map<String, String> headers;
  
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
  
  public static HttpResponse newInstance(int statusCode, byte[] content, Map<String, String> headers)
  {
    if (statusCode < 100 || statusCode > 599) {
      throw new IllegalStateException(
          String.format("Invalid status code: %d", statusCode));
    }
    HttpResponse instance = RECYCLER.get();
    instance.statusCode = statusCode;
    instance.content = Objects.requireNonNullElse(content, EMPTY_BYTE_ARRAY);
    instance.headers = Objects.requireNonNullElse(headers, EMPTY_MAP);
    return instance;
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
  public byte[] content()
  {
    return content;
  }
  
  /**
   * @return The response headers as a String -> String map Returns the underlining
   * Map instance and therefor any changes will mutate the response.
   */
  public Map<String, String> headers()
  {
    return headers;
  }
  
  @Override
  public boolean recycle()
  {
    statusCode = 0;
    content = null;
    headers = null;
    handle.recycle(this);
    return true;
  }
}
