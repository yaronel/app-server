package com.appsflyer.rta.appserver;

import com.appsflyer.rta.appserver.executor.ExecutorConfig;

/**
 * Categorizes the way the handler is executed
 * <p></p>
 * <b>NON_BLOCKING</b> - The handling of the request is non-blocking in nature, and as
 * such the handler will return a {@link java.util.concurrent.CompletableFuture}
 * of an {@link HttpResponse}. The handler will be called on the IO event loop,
 * and therefor is expected not to block the thread.
 * <p></p>
 * <b>ASYNC</b> - The handler will be called on a separate worker thread pool.
 * The size and name of the pool can be configured by calling {@link ServerConfig.Builder#setAsyncExecutorsConfig(ExecutorConfig)}.
 * Note that the thread pool used for request
 * processing is bounded. It is recommended to keep blocking code to a bare minimum
 * when throughput and latency are important.
 * <p></p>
 * <b>SYNC</b> - The handler will be called on the IO event loop thread and
 * is expected to not block the calling thread. "Non blocking" refers to CPU bound
 * processing (as opposed to IO processing), that doesn't take a "long time" to
 * complete (an order of magnitude of less than 1 millisecond).
 */
public enum ExecutionMode
{
  SYNC,
  ASYNC,
  NON_BLOCKING
}
