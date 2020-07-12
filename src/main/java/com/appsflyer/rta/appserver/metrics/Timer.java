package com.appsflyer.rta.appserver.metrics;

public interface Timer
{
  Timer start();
  
  long stop();
  
  long elapsed();
}
