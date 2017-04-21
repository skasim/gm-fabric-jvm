package com.deciphernow.server.security.support


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
