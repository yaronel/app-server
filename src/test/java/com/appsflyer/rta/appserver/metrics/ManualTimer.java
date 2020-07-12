package com.appsflyer.rta.appserver.metrics;

import java.util.function.Supplier;

public class ManualTimer extends Stopper
{
  private final Supplier<Long> tickSupplier;
  
  public ManualTimer(Supplier<Long> tickSupplier)
  {
    this.tickSupplier = tickSupplier;
  }
  
  public long tick()
  {
    return tickSupplier.get();
  }
  
  @Override
  public boolean recycle()
  {
    return true;
  }
  
}
