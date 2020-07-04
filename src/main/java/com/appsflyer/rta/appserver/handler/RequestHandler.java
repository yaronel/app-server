package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface RequestHandler extends Function<HttpRequest, HttpResponse>
{
  @SuppressWarnings("AbstractMethodOverridesAbstractMethod")
  @Override
  HttpResponse apply(HttpRequest request);
  
  CompletableFuture<HttpResponse> applyAsync(HttpRequest request);
}
