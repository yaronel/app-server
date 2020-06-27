package com.appsflyer.af_netty.handler;

import com.appsflyer.af_netty.HttpResponse;
import com.appsflyer.af_netty.request.HttpRequest;

import java.util.function.Function;

public interface RequestHandler extends Function<HttpRequest, HttpResponse>
{
}
