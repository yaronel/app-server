# RTA Application Server

Minimalistic API over [Netty](https://netty.io) HTTP server that makes creating 
simple application servers easy.

### API

The library abstracts the creation of an HTTP server using a server configuration  
builder - [ServerConfig.Builder](src/main/java/com/github/yaronel/appserver/ServerConfig.java).
The configuration is used to initialise an implementation of the 
[HttpServer](src/main/java/com/github/yaronel/appserver/HttpServer.java) interface 
that lets the user start and stop the server. Part of being minimalistic means the 
implementation is opinionated, and there isn't much left for the user to configure 
when it comes to codecs that can control lower level aspects of the request / response flow.

### RequestHandler

The main "ingredient" the user provides is an implementation of the 
[RequestHandler](src/main/java/com/github/yaronel/appserver/handler/RequestHandler.java) 
interface. Users may implement either `apply` or `applyAsyc`, depending on the 
[ExecutionMode](src/main/java/com/github/yaronel/appserver/ExecutionMode.java) 
they configure, with the application business logic. 

### Execution Modes

There are three execution-modes that describe the handler will be executed:
- `ExecutionMode.NON_BLOCKING` - The handling of the request is non-blocking in nature, and as
such the handler will return a `CompletableFuture` of an `HttpResponse`. 
The handler will be called on the IO event loop, and therefor is expected not 
to block the thread.
- `ExecutionMode.SYNC` - The handler will be called on the IO event loop thread and
is expected to not block the calling thread. "Non blocking" refers to CPU bound
processing (as opposed to IO processing), that doesn't take a "long time" to
complete (an order of magnitude of less than 1 millisecond).
- `ExecutionMode.ASYNC` - The handler will be called on a separate worker thread pool. 
It is recommended to keep blocking code to a bare minimum when throughput and latency are important.

### Metrics 

Users of the library can opt to record latency, throughput, and bandwidth metrics 
using [dropwizard metrics](https://metrics.dropwizard.io/4.1.2/).
@TODO: Implement dropwizard integration.
The related classes can be found under the 
[metrics](src/main/java/com/github/yaronel/appserver/metrics) package.

### Logging

The library uses [SLF4J](http://www.slf4j.org/) as its logging facade. The application 
may use any compatible logging library. 
