package com.appsflyer.rta.httpserver.handler;

import com.appsflyer.rta.httpserver.HttpResponse;
import com.appsflyer.rta.httpserver.request.HttpRequest;

/**
 * User supplied synchronous request handler.
 * The handler will be called with a single {@link HttpRequest} argument
 * and should return an {@link HttpResponse}
 */
@FunctionalInterface
public interface SyncRequestHandler extends RequestHandler
{
}
