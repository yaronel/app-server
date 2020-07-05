package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;
import com.appsflyer.rta.appserver.codec.FullHtmlRequestDecoder;
import com.appsflyer.rta.appserver.codec.FullHtmlResponseEncoder;
import com.appsflyer.rta.appserver.metrics.MetricsCollector;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@Tag("slow")
class AsyncRequestHandlerTest
{
  private EmbeddedChannel channel;
  
  @BeforeEach
  void setUp()
  {
    channel = new EmbeddedChannel();
  }
  
  @AfterEach
  void tearDown()
  {
    channel.finishAndReleaseAll();
  }
  
  @Test
  void executesHandlerAsynchronously() throws InterruptedException
  {
    /*
     * **** Setup ****
     */
    var latch = new CountDownLatch(1);
    
    RequestHandler handler = new StubHandler(
        (request) -> HttpResponse.newInstance(
            200,
            (request.asString() + " world!").getBytes(UTF_8),
            Map.of("Content-Type", "text/plain")), latch);
    
    channel.pipeline()
           .addLast(FullHtmlRequestDecoder.INSTANCE)
           .addLast(FullHtmlResponseEncoder.INSTANCE)
           .addLast(new AsyncRequestHandler(handler, mock(MetricsCollector.class)));
    
    FullHttpRequest request = new DefaultFullHttpRequest(
        HTTP_1_1, POST, "http://localhost", Unpooled.wrappedBuffer("Hello".getBytes(UTF_8)));
    
    /*
     * **** Execution ****
     */
    channel.writeInbound(request);
    
    if (!latch.await(1, TimeUnit.SECONDS)) {
      fail("Operation exceeded 1 second.");
    }
    
    /*
     * **** Assertions ****
     */
    var msg = channel.readOutbound();
    assertThat(msg, instanceOf(FullHttpResponse.class));
    var response = (FullHttpResponse) msg;
    assertEquals(200, response.status().code());
    assertEquals("Hello world!", response.content().toString(UTF_8));
    assertEquals("text/plain", response.headers().get(CONTENT_TYPE));
  }
  
  class StubHandler implements RequestHandler
  {
    private final Function<? super HttpRequest, HttpResponse> task;
    private final CountDownLatch latch;
    
    StubHandler(Function<? super HttpRequest, HttpResponse> task, CountDownLatch latch)
    {
      this.task = task;
      this.latch = latch;
    }
    
    @Override
    public CompletableFuture<HttpResponse> applyAsync(HttpRequest request)
    {
      return CompletableFuture.supplyAsync(
          () -> {
            var response = task.apply(request);
            channel.writeOutbound(response);
            latch.countDown();
            return response;
          },
          CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS));
    }
    
    @Override
    public HttpResponse apply(HttpRequest request)
    {
      //noinspection ReturnOfNull
      return null;
    }
  }
}
