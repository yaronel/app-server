package com.appsflyer.af_netty.handler;

import com.appsflyer.af_netty.HttpResponse;
import com.appsflyer.af_netty.request.HttpRequest;

/**
 * User supplied synchronous request handler.
 * The handler will be called with a single {@link HttpRequest} argument
 * and should return an {@link HttpResponse}
 */
public interface SyncRequestHandler extends RequestHandler
{
}
