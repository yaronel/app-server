package com.github.yaronel.appserver;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.Recycler;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class Headers implements Recyclable
{
  private final Recycler.Handle<Headers> handle;
  private HttpHeaders impl;
  
  private static final Recycler<Headers> RECYCLER = new Recycler<>()
  {
    protected Headers newObject(Recycler.Handle<Headers> handle)
    {
      return new Headers(handle);
    }
  };
  
  static Headers newInstance(HttpHeaders impl)
  {
    Headers headers = RECYCLER.get();
    headers.impl = impl;
    return headers;
  }
  
  private Headers(Recycler.Handle<Headers> handle)
  {
    this.handle = handle;
  }
  
  public Map<String, String> getAll()
  {
    Map<String, String> res = new HashMap<>(impl.size());
    var iter = impl.iteratorAsString();
    while (iter.hasNext()) {
      var entry = iter.next();
      res.put(entry.getKey().toLowerCase(Locale.US), entry.getValue());
    }
    
    return res;
  }
  
  @Override
  public boolean recycle()
  {
    impl = null;
    handle.recycle(this);
    return true;
  }
  
}
