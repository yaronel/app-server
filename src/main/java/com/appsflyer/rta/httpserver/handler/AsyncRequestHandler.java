package com.appsflyer.rta.httpserver.handler;

import com.appsflyer.rta.httpserver.HttpResponse;
import com.appsflyer.rta.httpserver.request.HttpRequest;

import java.util.concurrent.CompletableFuture;

/**
 * User supplied asynchronous request handler.
 * The handler will be called with a single {@link HttpRequest} argument
 * and should return a {@link java.util.concurrent.CompletableFuture< HttpResponse >}
 */
@FunctionalInterface
public interface AsyncRequestHandler extends RequestHandler<CompletableFuture<HttpResponse>>
{
}
