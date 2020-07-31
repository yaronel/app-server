package com.github.yaronel.appserver.handler;

import com.github.yaronel.appserver.HttpRequest;
import com.github.yaronel.appserver.HttpResponse;
import com.github.yaronel.appserver.metrics.MetricsCollector;
import com.github.yaronel.appserver.metrics.SystemClock;
import com.github.yaronel.appserver.metrics.TimeProvider;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("WeakerAccess")
public class CompletableRequestHandler extends RequestHandlerAdapter
{
  private static final Logger logger = LoggerFactory.getLogger(CompletableRequestHandler.class.getName());
  private final UserRequestHandler<HttpRequest, HttpResponse> requestHandler;
  private final MetricsCollector metricsCollector;
  private final TimeProvider clock;
  
  public CompletableRequestHandler(
      UserRequestHandler<HttpRequest, HttpResponse> requestHandler,
      MetricsCollector metricsCollector)
  {
    this.requestHandler = requestHandler;
    this.metricsCollector = metricsCollector;
    this.clock = new SystemClock();
  }
  
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
  {
    HttpRequest request = (HttpRequest) msg;
    long start = clock.time();
    
    // Slightly long lambda is unavoidable here to maintain references in scope
    //noinspection OverlyLongLambda
    exec(request)
        .handle((response, throwable) -> {
          metricsCollector.recordServiceLatency(clock.time() - start);
          if (throwable == null) {
            ctx.writeAndFlush(response, ctx.voidPromise());
          }
          else {
            exceptionCaught(ctx, throwable);
          }
          request.recycle();
          //noinspection ReturnOfNull
          return null;
        });
  }
  
  private CompletableFuture<HttpResponse> exec(HttpRequest request)
  {
    try {
      return requestHandler.applyAsync(request);
    } catch (RuntimeException ex) {
      return CompletableFuture.failedFuture(ex);
    }
  }
  
  @Override
  public Logger logger()
  {
    return logger;
  }
}
