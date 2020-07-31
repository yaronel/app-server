package com.github.yaronel.appserver.handler;

import java.util.concurrent.CompletableFuture;

public interface UserRequestHandler<T, S>
{
  S apply(T request);
  
  CompletableFuture<S> applyAsync(T request);
}
