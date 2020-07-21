package com.appsflyer.rta.appserver.handler;

import com.appsflyer.rta.appserver.ExecutionMode;
import com.appsflyer.rta.appserver.HttpRequest;
import com.appsflyer.rta.appserver.HttpResponse;
import com.appsflyer.rta.appserver.ServerConfig;
import com.appsflyer.rta.appserver.codec.FullHtmlRequestDecoder;
import com.appsflyer.rta.appserver.codec.FullHtmlResponseEncoder;
import com.appsflyer.rta.appserver.metrics.MetricsCollectorFactory;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.appsflyer.rta.appserver.TestUtil.requestWithContent;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("slow")
class SyncRequestHandlerTest
{
  @SuppressWarnings("StaticVariableMayNotBeInitialized")
  private static ServerConfig config;
  private EmbeddedChannel channel;
  
  @BeforeAll
  static void beforeAll()
  {
    config = mock(ServerConfig.class);
    when(config.mode()).thenReturn(ExecutionMode.SYNC);
    when(config.metricsCollector()).thenReturn(MetricsCollectorFactory.NOOP);
  }
  
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
  void executesHandlerSynchronously()
  {
    when(config.requestHandler()).thenReturn(new StubHandler());
  
    channel.pipeline()
           .addLast(FullHtmlRequestDecoder.INSTANCE)
           .addLast(FullHtmlResponseEncoder.INSTANCE)
           .addLast(RequestHandlerFactory.newInstance(config));
  
    channel.writeInbound(requestWithContent("Hello"));
  
    var msg = channel.readOutbound();
    assertThat(msg, instanceOf(FullHttpResponse.class));
    var response = (FullHttpResponse) msg;
    assertEquals(200, response.status().code());
    assertEquals("Hello world!", response.content().toString(UTF_8));
    assertEquals("text/plain", response.headers().get(CONTENT_TYPE));
  }
  
  @Test
  void returnsInternalServerErrorWhenExceptionIsThrown()
  {
    when(config.requestHandler()).thenReturn(exceptionalStubHandler());
  
    channel.pipeline().addLast(RequestHandlerFactory.newInstance(config));
  
    channel.writeInbound(HttpRequest.newInstance(requestWithContent(""), channel));
  
    var msg = channel.readOutbound();
    assertThat(msg, instanceOf(HttpResponse.class));
    var response = (HttpResponse) msg;
    assertEquals(500, response.statusCode());
    assertEquals("Internal Server Error", new String(response.content(), UTF_8));
    assertEquals("text/plain", response.headers().get(CONTENT_TYPE.toString()));
    
    response.recycle();
  }
  
  private static RequestHandler<HttpRequest, HttpResponse> exceptionalStubHandler()
  {
    return new StubHandler()
    {
      @Override
      public HttpResponse apply(HttpRequest request)
      {
        throw new RuntimeException();
      }
    };
  }
  
  static class StubHandler implements RequestHandler<HttpRequest, HttpResponse>
  {
    
    @Override
    public HttpResponse apply(HttpRequest request)
    {
      return HttpResponse.newInstance(
          200,
          (request.asString() + " world!").getBytes(UTF_8),
          Map.of("Content-Type", "text/plain"));
    }
    
    @Override
    public CompletableFuture<HttpResponse> applyAsync(HttpRequest request)
    {
      //noinspection ReturnOfNull
      return null;
    }
  }
}
