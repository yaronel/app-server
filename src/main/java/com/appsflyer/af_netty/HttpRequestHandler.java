package com.appsflyer.af_netty;

import java.util.function.Function;

public interface HttpRequestHandler extends Function<HttpRequest, HttpResponse>
{
}
