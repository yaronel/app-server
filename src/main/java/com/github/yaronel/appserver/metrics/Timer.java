package com.github.yaronel.appserver.metrics;

public interface Timer
{
  Timer start();
  
  long stop();
  
  long elapsed();
}
