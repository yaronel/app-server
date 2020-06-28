package com.appsflyer.rta.httpserver.request;

import com.appsflyer.rta.httpserver.Recyclable;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.Recycler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Headers implements Recyclable
{
  private final Recycler.Handle<Headers> handle;
  private final Object mutex = new Object();
  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private HttpHeaders impl;
  private volatile Map<String, String> rawHeaders;
  
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
    if (rawHeaders == null) {
      synchronized (mutex) {
        if (rawHeaders == null) {
          Map<String, String> res = new HashMap<>(impl.size());
          var iter = impl.iteratorAsString();
          while (iter.hasNext()) {
            var entry = iter.next();
            res.put(entry.getKey(), entry.getValue());
          }
          rawHeaders = res;
        }
      }
    }
    return Collections.unmodifiableMap(rawHeaders);
  }
  
  @Override
  public boolean recycle()
  {
    impl = null;
    rawHeaders = null;
    handle.recycle(this);
    return true;
  }
  
}
