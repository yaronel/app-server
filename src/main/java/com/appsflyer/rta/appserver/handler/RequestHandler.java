package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;

import java.util.function.Function;

@FunctionalInterface
public interface RequestHandler extends Function<HttpRequest, HttpResponse>
{
}
