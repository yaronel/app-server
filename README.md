# RTA Application Server

Minimalistic API over [Netty](https://netty.io) HTTP server that makes creating 
simple application servers easy.

### API

The library abstracts the creation of an HTTP server using a server configuration  
builder - [ServerConfig.Builder](src/main/java/com/appsflyer/rta/appserver/ServerConfig.java).
The configuration is used to initialise an implementation of the 
[HttpServer](src/main/java/com/appsflyer/rta/appserver/HttpServer.java) interface 
that lets the user start and stop the server. Part of being minimalistic means the 
implementation is opinionated, and there isn't much left for the user to configure 
when it comes to codecs that can control lower level aspects of the request / response flow.

### RequestHandler

The main "ingredient" the user provides is an implementation of the 
[RequestHandler](src/main/java/com/appsflyer/rta/appserver/handler/RequestHandler.java) 
interface. Users may implement either `apply` or `applyAsyc`, depending on the 
[HandlerMode](src/main/java/com/appsflyer/rta/appserver/HandlerMode.java) 
they configure, with the application business logic. 

### Handler Modes

There are three handler-modes that categorise the business logic handling users 
can choose from:
- `HandleMode.NON_BLOCKING` - The handler does not perform any blocking IO operations, 
or any long computations. It is categorised as CPU bound. The handler will be executed 
on the server's connection handling event loop. It must implement `RequestHandler.apply`.
- `HandleMode.BLOCKING` - The handler performs some sort of blocking IO operation, 
or long computation. The user should configure a blocking executor thread pool 
where the handler will be executed. It must implement `RequestHandler.apply`.
- `HandleMode.ASYNC` - The handler is responsible for executing the application code
on its own thread pool. It is expected to return a 
[CompletableFuture](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
<[HttpResponse](src/main/java/com/appsflyer/rta/appserver/HttpResponse.java)> 
immediately without blocking. It must implement `RequestHandler.applyAsync`.

### Metrics 

Users of the library can opt to record latency, throughput, and bandwidth metrics 
using [metrics-ng](https://gitlab.appsflyer.com/clojure/af-metrics-ng).
The related classes can be found under the 
[metrics](src/main/java/com/appsflyer/rta/appserver/metrics) package.

### Logging

The library uses [SLF4J](http://www.slf4j.org/) as its logging facade. The application 
may use any compatible logging library. 
