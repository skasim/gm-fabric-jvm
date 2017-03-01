package com.deciphernow.server.security.support

import java.security.cert.X509Certificate

import com.deciphernow.server.tls.TlsConfigUtil
import com.twitter.util.Await
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.{TrustStrategy, SSLConnectionSocketFactory}
import org.apache.http.impl.client.{HttpClients, CloseableHttpClient}
import java.io.File
/**
  * Created by ghershfield on 6/2/16.
  */
class RestClient {

  val sslContext = TlsConfigUtil.createSslContext(
    new File("src/test/resources/keystore.jks"),
    "password",
    new File("src/test/resources/truststore.jks"),
    "password"
  )
  val tlsTypes = Array("TLSv1.2", "SSLv3")
  val sslConnectionSocketFactory = new SSLConnectionSocketFactory(
    sslContext,
    tlsTypes,
    null,
    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
  )
  val trustStrategy = new TrustStrategy {
    override def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = true
  }
  def execute : Unit = {
    val httpClient : CloseableHttpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build()
    val httpGet = new HttpGet("") // todo: fix this.
    val response = httpClient.execute(httpGet)
    println("execute response => " + response)
  }
}
