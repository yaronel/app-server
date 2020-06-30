package com.appsflyer.rta.httpserver.handler;

import com.appsflyer.rta.httpserver.request.HttpRequest;

import java.util.function.Function;

@FunctionalInterface
public interface RequestHandler<R> extends Function<HttpRequest, R>
{
}
