package com.github.yaronel.appserver.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ManualClock implements TimeProvider
{
  private final AtomicLong time;
  private final AtomicInteger callCounter;
  private Consumer<ManualClock> onTimeHandler;
  
  public ManualClock()
  {
    time = new AtomicLong(0);
    callCounter = new AtomicInteger(0);
  }
  
  /**
   * Add an event handler that will be called when {@link ManualClock#time} is called.
   *
   * @param handler The handler will receive the instance as an argument.
   */
  public void onTime(Consumer<ManualClock> handler)
  {
    this.onTimeHandler = handler;
  }
  
  @Override
  public long time()
  {
    if (onTimeHandler != null) {
      onTimeHandler.accept(this);
    }
    callCounter.incrementAndGet();
    return time.get();
  }
  
  public long advanceClock(int amount)
  {
    return time.addAndGet(amount);
  }
  
  public long advanceClock()
  {
    return advanceClock(1);
  }
  
  public int calls()
  {
    return callCounter.get();
  }
  
}
