package com.appsflyer.af_netty.request;

import com.appsflyer.af_netty.HttpResponse;

import java.util.function.Function;

public interface HttpRequestHandler extends Function<HttpRequest, HttpResponse>
{
}
