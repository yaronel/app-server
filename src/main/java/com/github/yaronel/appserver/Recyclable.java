package com.github.yaronel.appserver;

/**
 * The interface represents a pooled object that can be recycled and reused.
 * Implementing classes must free up any state variables associated with the
 * instance and return it to the pool.
 * <p>
 * Clients of the interface must call {@link Recyclable#recycle()} when they
 * are finished using it.
 * <p>
 * For more information regarding Netty's thread-local object pools
 * see {@link io.netty.util.Recycler} and
 * <a href="https://github.com/netty/netty/wiki/Using-as-a-generic-library#thread-local-object-pool">this wiki</a>
 */
@SuppressWarnings("SameReturnValue")
@FunctionalInterface
public
interface Recyclable
{
  boolean recycle();
}
