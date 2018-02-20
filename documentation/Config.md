# Overview
This page describes how to add in your own `attributes` to your service.

## Generation
Apon creation of your service, a `config` module is created with commented out templated code that looks as follows:


    package com.acme.config
    
    import com.twitter.app.{GlobalFlag, GlobalFlagVisible}
    import com.deciphernow.server.Implicits.stringOptionFlaggable
    
    //object anObjectFlag extends GlobalFlag[String]("HasSomeValue","Put description here.")
    
    //package somePackage {
    //  object anotherObjectFlag  extends GlobalFlag[String]("AnotherValue", "Put description here.")
    //}
    
    
### Example of adding capability
We are going to add an attribute to our newly created __myfirstmicroserivce__.   Edit __com.acme.config.Config__ and add __object version__, see below:


    package com.acme.config
    
    import com.twitter.app.{GlobalFlag, GlobalFlagVisible}
    import com.deciphernow.server.Implicits.stringOptionFlaggable
    import com.deciphernow.server.config.rest
    
    object version extends GlobalFlag[String]("0.0.0","Unassigned version returns 0.0.0")


### Scala reference
In the business module, lets edit __src/main/scala/com.acme.MyFirstMicroserviceManager__ and modify the __getPong__ method to the following:

    package com.acme
    
    /**
     *
     */
    class MyFirstMicroserviceManager {
    
      /**
        * Keep this. Use to see if the service is accepting connections.
        * @return
        */
      def getPong = {
        println("My added attribute : " + com.acme.config.version.apply)
        "pong\n"
      }
    }
 
Compile your service, fix the user issue, start it, hit the ping end-point. The print statement is going to echo the default value of __version__ to the output when you started the service with __console__. This is because we did __NOT__ assign a value in __parameters.config__.
[Reference Creating New Microservice if you forgot](CreatingNewMS.md)

    
    jvm 1    | 15:02:24.994 [WrapperSimpleAppMain] INFO  c.t.finatra.http.routing.HttpRouter - Adding routes
    jvm 1    | GET     /ping
    jvm 1    | 15:02:24.997 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer -  => com.twitter.server.handler.AdminRedirectHandler
    jvm 1    | 15:02:24.997 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin => com.twitter.server.handler.SummaryHandler
    jvm 1    | 15:02:24.997 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/server_info => com.twitter.finagle.Filter$$anon$1
    jvm 1    | 15:02:24.997 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/contention => com.twitter.finagle.Filter$$anon$1
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/lint => com.twitter.server.handler.LintHandler
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/lint.json => com.twitter.server.handler.LintHandler
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/failedlint => com.twitter.server.handler.FailedLintRuleHandler
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/threads => com.twitter.server.handler.ThreadsHandler
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/threads.json => com.twitter.server.handler.ThreadsHandler
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/announcer => com.twitter.finagle.Filter$$anon$1
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/dtab => com.twitter.finagle.Filter$$anon$1
    jvm 1    | 15:02:24.998 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/pprof/heap => com.twitter.server.handler.HeapResourceHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/pprof/profile => com.twitter.server.handler.ProfileResourceHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/pprof/contention => com.twitter.server.handler.ProfileResourceHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/ping => com.twitter.server.handler.ReplyHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/shutdown => com.twitter.server.handler.ShutdownHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/tracing => com.twitter.server.handler.TracingHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/events => com.twitter.server.handler.EventsHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/events/record/ => com.twitter.server.handler.EventRecordingHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/logging => com.twitter.server.handler.LoggingHandler
    jvm 1    | 15:02:24.999 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/metrics => com.twitter.server.handler.MetricQueryHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/clients/ => com.twitter.server.handler.ClientRegistryHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/servers/ => com.twitter.server.handler.ServerRegistryHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/files/ => com.twitter.server.handler.ResourceHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/registry.json => com.twitter.server.handler.RegistryHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/toggles => com.twitter.server.handler.ToggleHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/toggles/ => com.twitter.server.handler.ToggleHandler
    jvm 1    | 15:02:25.000 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /favicon.ico => com.twitter.server.handler.ResourceHandler
    jvm 1    | 15:02:25.001 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/histograms => com.twitter.server.handler.HistogramQueryHandler
    jvm 1    | 15:02:25.001 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - /admin/histograms.json => com.twitter.server.handler.HistogramQueryHandler
    jvm 1    | 15:02:25.020 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - http server started on port: 8888
    jvm 1    | 15:02:25.021 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - Enabling health endpoint on port 9990
    jvm 1    | 15:02:25.022 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - com.deciphernow.server.rest.GMFabricRestServer started.
    jvm 1    | 15:02:25.023 [WrapperSimpleAppMain] INFO  c.d.server.rest.GMFabricRestServer - Startup complete, server ready.
    jvm 1    | My added attribute : 0.0.0
    jvm 1    | 15:02:37.335 [finagle/netty3-1] INFO  c.t.f.h.filters.AccessLoggingFilter - 127.0.0.1 - - [26/Jan/2018:20:02:37 +0000] "GET /ping HTTP/1.1" 200 5 10 "curl/7.55.1"


### Java reference
When referencing the attributes in java casting to the value type is required.

    String version = (String)com.acme.config.version.apply();
    

## Word to the wise
Make sure to instantiate any classes that use attributes, outside of the server, last within the premain for example __MyFirstMicroserviceManager__.

      premain {
    
        //accessManager = new FileWhitelistImpersonationAccessManager(
        //   whitelistFile(), Duration(1, "minute")
        //)
    
        //
        // Decryptor plugin - retrieve instance here.
        // val decryptor = DecryptorManager.getInstance
    
        //
        // Instantiate all business logic after the decryptor instantiation.
        myFirstMicroserviceManager = new MyFirstMicroserviceManager
      }

If classes are instantiated before the __premain__ and their attributes are being called within the code, they will always return the __DEFAULT__ value. 