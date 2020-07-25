package com.github.yaronel.appserver.metrics;

import com.github.yaronel.appserver.Recyclable;
import io.netty.util.Recycler;

/**
 * Stopper style implementation of a timer.
 * The class is part of an object pool and therefor it is safe to create
 * many instances and discard them without worrying about GC effects.
 * <p>
 * The class is <i>not</i> thread safe.
 */
public class Stopper implements Timer, Recyclable
{
  private Recycler.Handle<Stopper> handle;
  private long start;
  private long stop;
  private boolean started;
  
  private static final Recycler<Stopper> RECYCLER = new Recycler<>()
  {
    protected Stopper newObject(Recycler.Handle<Stopper> handle)
    {
      return new Stopper(handle);
    }
  };
  
  
  public static Stopper newInstance()
  {
    return RECYCLER.get();
  }
  
  public static Stopper newStartedInstance()
  {
    return RECYCLER.get().start();
  }
  
  /* For tests */
  Stopper()
  {
  }
  
  private Stopper(Recycler.Handle<Stopper> handle)
  {
    this.handle = handle;
  }
  
  /**
   * Starts the timer.
   * Method is idempotent.
   */
  @Override
  public Stopper start()
  {
    if (!started) {
      stop = 0;
      started = true;
      start = tick();
    }
    return this;
  }
  
  /**
   * Stops the timer and returns the duration that elapsed since the timer started.
   * Method is idempotent.
   */
  @Override
  public long stop()
  {
    if (started) {
      stop = tick();
      started = false;
    }
    return elapsed();
  }
  
  /**
   * Returns the time elapsed since the timer started.
   * Does <i>not</i> stop the timer.
   */
  @Override
  public long elapsed()
  {
    if (started) {
      return tick() - start;
    }
    return stop - start;
  }
  
  long tick()
  {
    return System.nanoTime();
  }
  
  @Override
  public boolean recycle()
  {
    start = 0;
    stop = 0;
    started = false;
    handle.recycle(this);
    return true;
  }
}
