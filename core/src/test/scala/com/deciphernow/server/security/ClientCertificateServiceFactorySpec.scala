package com.deciphernow.server.security


/*
    Copyright 2017 Decipher Technology Studios LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import java.io.File
import java.math.BigInteger
import java.net.{InetSocketAddress, SocketAddress}
import java.security.{Principal, PublicKey}
import java.util.Date
import javax.net.ssl.{SSLEngine, SSLContext}
import javax.security.cert.X509Certificate
import com.deciphernow.server.security.support.{CertificateDNHelperFilter, Hello}
import com.deciphernow.server.security.support.Hello.FutureIface
import com.deciphernow.server.thrift.ThriftUtil
import com.deciphernow.server.tls.TlsConfigUtil
import com.twitter.finagle.ssl.{Ssl, Engine}
import com.twitter.finagle.thrift.Protocols
import com.twitter.finagle.transport.Transport
import com.twitter.finagle.{Thrift, ClientConnection, Service}
import com.twitter.util.{Await, Time, Future}
import Hello.FutureIface
import org.scalatest.{Matchers, FlatSpec}

import scala.io.Source

/**
  *
  */
class ClientCertificateServiceFactorySpec extends FlatSpec with Matchers {

  val service = new Service[String, Option[UserAuthentication]] {
    def apply(req: String) = {
      Future.value(UserAuthentication.current)
    }
  }

  val serviceFactory: ClientCertificateServiceFactory[String, Option[UserAuthentication]]
        = new ClientCertificateServiceFactory[String, Option[UserAuthentication]](service)

  /**
    * No Certificate provided.
    */
  "A ClientCertificateServiceFactory" should "set None for UserAuthentication when no X509 certificate" in {
    val connection: MockClientConnection = new MockClientConnection(None)
    val svc: Future[Service[String, Option[UserAuthentication]]] = serviceFactory.apply(connection)

    val respFuture : Future[Option[UserAuthentication]] = svc.flatMap { value =>
      value.apply("req")
    }
    val response = respFuture map { resp => resp }
    response.onSuccess(code => code should equal(None))
  }

  /**
    *
    * @return
    */
  def createSslContext() : SSLContext = {
    val keystore = new File(getClass.getResource("/keystore.jks").getFile)
    val truststore = new File(getClass.getResource("/truststore.jks").getFile)
    TlsConfigUtil.createSslContext(keystore, "password", truststore, "password")
  }

  /**
    * Create SSLEngine
    *
    * @return
    */
  def createSslEngine() : SSLEngine = {
    val engine : SSLEngine = createSslContext().createSSLEngine()
    engine.setNeedClientAuth(true)
    engine.setUseClientMode(false)
    engine
  }

  /**
    * Create TwitterTLSEngine
    *
    * @return
    */
  def createTwitterTLSEngine : Engine = {
    new Engine(createSslEngine())
  }

}

/* ************************************************************************************************** */

/**
  * These mock classes will only work for No SSL testing; empty SSL.
  * This is because where the certificate is retrieved from is deeper
  * in the network stack.
  *
  * @param cert
  */
class MockClientConnection(cert: Option[X509Certificate]) extends ClientConnection {
  def clientCertificate: Option[X509Certificate] = cert

  override def remoteAddress: SocketAddress = ???

  override def onClose: Future[Unit] = ???

  override def localAddress: SocketAddress = ???

  override def close(deadline: Time): Future[Unit] = ???
}

class MockX509Certificate(name: String) extends X509Certificate {
  override def checkValidity(): Unit = ???

  override def getSerialNumber: BigInteger = ???

  override def checkValidity(date: Date): Unit = ???

  override def getSigAlgOID: String = ???

  override def getSigAlgParams: Array[Byte] = ???

  override def getVersion: Int = ???

  override def getNotBefore: Date = ???

  override def getSubjectDN: Principal = new Principal {
    override def getName: String = name
  }

  override def getIssuerDN: Principal = ???

  override def getSigAlgName: String = ???

  override def getNotAfter: Date = ???

  override def verify(key: PublicKey): Unit = ???

  override def verify(key: PublicKey, sigProvider: String): Unit = ???

  override def getEncoded: Array[Byte] = ???

  override def getPublicKey: PublicKey = ???

  override def toString: String = name
}
