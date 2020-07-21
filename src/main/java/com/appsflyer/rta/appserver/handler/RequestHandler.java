package com.appsflyer.rta.appserver.handler;

import java.util.concurrent.CompletableFuture;

public interface RequestHandler<T, S>
{
  S apply(T request);
  
  CompletableFuture<S> applyAsync(T request);
}
