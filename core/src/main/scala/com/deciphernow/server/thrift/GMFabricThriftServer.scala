package com.deciphernow.server.thrift

import java.net.InetSocketAddress

import com.deciphernow.server.support.{DecryptorManager, SupportUtils}
import com.deciphernow.server.tls.TlsServerUtil
import com.twitter.finagle.thrift.Protocols
import com.twitter.inject.app.App
import com.twitter.finagle.transport.Transport
import com.twitter.finagle.{ListeningServer, SimpleFilter, Thrift}
import com.twitter.logging.Logger
import com.deciphernow.server.{config => configuration}
import com.deciphernow.server.security.ClientCertificateServiceFactory
import com.deciphernow.server.support.{Decryptor, DecryptorManager}


/**
  *
  * @param filters
  * @param service
  */
class GMFabricThriftServer(filters: Seq[SimpleFilter[Array[Byte], Array[Byte]]], service: AnyRef)
  extends App {

  val log = Logger.get(getClass)
  var server : Option[ListeningServer] = None
  val serverPrefixLabel = "thrift"

  def getServer = server

  override def main() : Unit = {

    // DO NOT create a thrift server.
    if ((defaultThriftPort == "") || (defaultThriftPort == ":")) {
      log.ifInfo("Thrift port has been configured such that NO THRIFT server will be instantiated.")
      return
    }
    val stateSsl = SupportUtils.isSsl
    val serverLabel = SupportUtils.createServerLabel(serverPrefixLabel,stateSsl)
    val statsFilter = new ThriftMethodStatsFilter(Protocols.binaryFactory(),"srv/" + serverLabel,"/method/")
    val newSequenceFilters = filters :+ statsFilter
    val compositeService = newSequenceFilters.foldRight(ThriftUtil.serverFromIface(service,Protocols.binaryFactory()))(_ andThen _)
    if (!stateSsl) {
      log.ifDebug("Creating thrift server without SSL.");
      server = Option(Thrift.server.withLabel(serverLabel)
                      .serve(new InetSocketAddress(defaultThriftPort.substring(1).toInt),
                      compositeService))
    }
    else {
      log.ifDebug("Creating thrift server with 2-Way SSL.")
      val clientCertificateServiceFactory = new ClientCertificateServiceFactory[Array[Byte],Array[Byte]](compositeService)
      server = Option(Thrift.server.withLabel(serverLabel).configured(Transport.TLSServerEngine(Option(()=>{TlsServerUtil.createTlsEngine(
        configuration.tls.keyStore().get,
        decrypter.decryptResource(configuration.tls.keyStorePass()),
        configuration.tls.trustStore().get,
        decrypter.decryptResource(configuration.tls.trustStorePass())
      )}))).serve(new InetSocketAddress(defaultThriftPort.substring(1).toInt), clientCertificateServiceFactory))
    }
  }

  def defaultThriftPort: String = configuration.thrift.port()

  lazy val decrypter : Decryptor = DecryptorManager.getInstance
}
