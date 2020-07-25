package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpRequest;
import com.github.yaronel.appserver.HttpResponse;
import com.github.yaronel.appserver.metrics.MetricsCollector;

@SuppressWarnings("WeakerAccess")
public class AsyncRequestHandler extends SyncRequestHandler
{
  public AsyncRequestHandler(RequestHandler<HttpRequest, HttpResponse> requestHandler, MetricsCollector metricsCollector)
  {
    super(requestHandler, metricsCollector);
  }
}
