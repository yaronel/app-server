package com.appsflyer.rta.appserver;

/**
 * Categorizes the type of request handling.
 * <p>
 * <b>Async</b> - The handling of the request is asynchronous in nature, and as
 * such the handler will return a {@link java.util.concurrent.CompletableFuture}
 * of an {@link HttpResponse}. The handler will be called on the IO event loop,
 * and therefor is expected not to block the thread.
 * <p>
 * <b>NON_BLOCKING</b> - The handling of the request is non-blocking in nature.
 * "Non-blocking" refers to CPU bound processing (as opposed to IO processing),
 * that doesn't take a "long time" to complete (an order of magnitude of less
 * than 1 millisecond).
 * <p>
 * <b>BLOCKING</b> - The handling of the request is blocking in nature, and therefor
 * should not be handled on the IO event loop thread. The request will be offloaded
 * to another thread for processing. Note that the thread pool used for request
 * processing is bounded. It is recommended to keep blocking code to a bare minimum
 * when throughput and latency are important.
 */
public enum HandlerMode
{
  ASYNC,
  NON_BLOCKING,
  BLOCKING
}
