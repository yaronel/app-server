package com.appsflyer.af_netty;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.Recycler;

import java.util.HashMap;
import java.util.Map;

public class Headers implements Recyclable
{
  private final Recycler.Handle<Headers> handle;
  private final Object mutex = new Object();
  private HttpHeaders impl;
  private volatile Map<CharSequence, CharSequence> rawHeaders;
  
  private static final Recycler<Headers> RECYCLER = new Recycler<>()
  {
    protected Headers newObject(Recycler.Handle<Headers> handle)
    {
      return new Headers(handle);
    }
  };
  
  public static Headers newInstance(HttpHeaders impl)
  {
    Headers headers = RECYCLER.get();
    headers.impl = impl;
    return headers;
  }
  
  private Headers(Recycler.Handle<Headers> handle)
  {
    this.handle = handle;
  }
  
  public Map<CharSequence, CharSequence> getAll()
  {
    if (rawHeaders == null) {
      synchronized (mutex) {
        if (rawHeaders == null) {
          var res = new HashMap<CharSequence, CharSequence>(impl.size());
          var iter = impl.iteratorCharSequence();
          while (iter.hasNext()) {
            var entry = iter.next();
            res.put(entry.getKey(), entry.getValue());
          }
          rawHeaders = res;
        }
      }
    }
    return rawHeaders;
  }
  
  @Override
  public boolean recycle()
  {
    impl = null;
    handle.recycle(this);
    return true;
  }
  
}
