package com.appsflyer.af_netty;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class Headers
{
  private final HttpHeaders impl;
  
  public Headers(HttpHeaders impl) {this.impl = impl;}
  
  public String get(String name)
  {
    return impl.get(name);
  }
  
  public Map<CharSequence, CharSequence> getAll()
  {
    var res = new HashMap<CharSequence, CharSequence>(impl.size());
    impl.iteratorCharSequence()
        .forEachRemaining(entry -> res.put(entry.getKey(), entry.getValue()));
    return res;
  }
}
