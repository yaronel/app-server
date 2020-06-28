package com.appsflyer.rta.httpserver;

import java.net.UnknownHostException;

public interface HttpServer
{
  void start() throws InterruptedException, UnknownHostException;
  
  void awaitTermination();
  
  void stop();
}
