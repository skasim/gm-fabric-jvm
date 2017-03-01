package com.deciphernow.server.rest

import com.deciphernow.server.support.Decryptor
import com.deciphernow.server.tls.{TlsConfigUtil, TlsServerUtil}
import com.deciphernow.server.{config => configuration}
import com.deciphernow.server.support.{Decryptor, DecryptorManager}
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finagle.transport.{TlsConfig, Transport}
//import com.twitter.finagle.netty3.Netty3ListenerTLSConfig
import com.twitter.finagle.{Filter, Http}
import com.twitter.finatra.http.filters.{AccessLoggingFilter, ExceptionMappingFilter, StatsFilter}
import com.twitter.finatra.http.internal.server.BaseHttpServer
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.logging.Logger


/**
* Created by ghershfield on 4/18/16.
*/
/**
  *
  * @param filters
  * @param controllers
  */
class GMFabricRestServer(filters: Seq[Filter[FinagleRequest, FinagleResponse,FinagleRequest, FinagleResponse]], controllers: Seq[Controller])
  extends HttpServer {

  lazy override val log = Logger.get(getClass)

  /**
    * HTTP Port
    *
    * @return
    */
  override def defaultFinatraHttpPort: String = {
    configuration.rest.httpPort()
  }

  /**
    * HTTPS Port
    *
    * @return the HTTPS port if and only if JKS have been provided. Otherwise turn the port off.
    */
  override def defaultHttpsPort: String = {
    (configuration.tls.keyStore(),configuration.tls.trustStore()) match {
      case (Some(_),Some(_)) => configuration.rest.httpsPort()
      case (_,_) => ""
    }
  }
  /**
    * Admin port
    *
    * @return
    */
  override def defaultHttpPort: Int = configuration.admin.port().substring(1).toInt

  /**
    *
    * @return
    */
  override def failfastOnFlagsNotParsed: Boolean = true


  /**
    *
    * @param server
    * @return
    */
  override def configureHttpServer(server: Http.Server): Http.Server = {
    server
  }

  /**
    * Configure the server to be 2-way SSL enabled.
    * 
    * @param server
    * @return
    */
  override def configureHttpsServer(server: Http.Server): Http.Server = {
    server.configured(
      Transport.TLSServerEngine(Some(() => {
        TlsServerUtil.createTlsEngine(
          configuration.tls.keyStore().get,
          decrypter.decryptResource(configuration.tls.keyStorePass()),
          configuration.tls.trustStore().get,
          decrypter.decryptResource(configuration.tls.trustStorePass()))
      })
    ))
  }

  /**
    *
    * @param router
    */
  override protected def configureHttp(router: HttpRouter): Unit = {

    /**
      * Add custom filters here.
      */
    if (filters!=Nil) {
      log.ifDebug("Have filters to be added to the router.")
      for (filter <- filters) { router.filter(filter) }
    }
    else {
      log.ifDebug("There are no filters to be added to the router.")
    }

    /**
      * Add explicit Twitter filters directly here:
      *
      * Must always go before the controllers.
      */
    router.filter[AccessLoggingFilter[FinagleRequest]]
    //
    //    router.filter[HttpResponseFilter[FinagleRequest]] // this one forces a regular response to turn into a download. No idea why.
    //
    router.filter[ExceptionMappingFilter[FinagleRequest]]

    // Capture route stats per Controller.
    router.filter[StatsFilter[FinagleRequest]]

    /**
      * Add custom controllers here.
      */
    if (controllers!=Nil) {
      log.ifDebug("Have controllers to be added to the router.")
      for (controller <- controllers) {
        router.add(controller)
      }
    }
    else {
      log.ifInfo("There are no controllers to add to the router thus no capabilities for this service.")
    }

    router.exceptionMapper[FailFastExceptionMapper]
  }

  lazy val decrypter : Decryptor = DecryptorManager.getInstance
}