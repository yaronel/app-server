package com.github.yaronel.appserver;

import java.net.UnknownHostException;

public interface HttpServer
{
  void start() throws InterruptedException, UnknownHostException;
  
  void awaitTermination();
  
  void stop();
}
