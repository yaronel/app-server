package com.appsflyer.af_netty;

import java.util.Map;

public class HttpResponse
{
  private int statusCode;
  private byte[] content;
  private Map<String, String> headers;
  
  public static Builder newBuilder()
  {
    return new Builder(new HttpResponse());
  }
  
  private HttpResponse() {}
  
  public int statusCode()
  {
    return statusCode;
  }
  
  public byte[] content()
  {
    return content;
  }
  
  public Map<String, String> headers()
  {
    return headers;
  }
  
  public static class Builder
  {
    
    private HttpResponse instance;
    
    Builder(HttpResponse instance)
    {
      this.instance = instance;
    }
    
    public Builder setStatusCode(int code)
    {
      instance.statusCode = code;
      return this;
    }
    
    public Builder setContent(byte[] content)
    {
      instance.content = content;
      return this;
    }
    
    public Builder setHeaders(Map<String, String> headers)
    {
      instance.headers = headers;
      return this;
    }
    
    public HttpResponse build()
    {
      assertValidState();
      var res = instance;
      instance = null;
      return res;
    }
    
    private void assertValidState()
    {
      if (instance.statusCode < 100 || instance.statusCode > 599) {
        throw new IllegalStateException(
            String.format("Invalid status code: %d", instance.statusCode));
      }
      if (instance.content == null) {
        instance.content = new byte[0];
      }
      if (instance.headers == null) {
        instance.headers = Map.of();
      }
    }
  }
}
