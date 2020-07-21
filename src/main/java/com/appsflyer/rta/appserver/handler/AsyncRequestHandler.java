package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;

@SuppressWarnings("WeakerAccess")
public class AsyncRequestHandler extends SyncRequestHandler
{
  public AsyncRequestHandler(RequestHandler<HttpRequest, HttpResponse> requestHandler, MetricsCollector metricsCollector)
  {
    super(requestHandler, metricsCollector);
  }
}
