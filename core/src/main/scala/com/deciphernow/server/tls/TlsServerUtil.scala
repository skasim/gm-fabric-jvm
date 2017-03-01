package com.deciphernow.server.tls

import java.io.File
import javax.net.ssl.SSLEngine

import com.twitter.finagle.ssl.Engine

/**
  * Created by ghershfield on 5/15/16.
  */
object TlsServerUtil {

  /**
    *
    * @param keystoreFile
    * @param keystorePassword
    * @param truststoreFile
    * @param truststorePassword
    * @return
    */
  def createSslEngine(keystoreFile: File, keystorePassword: String,
                      truststoreFile: File, truststorePassword: String) : SSLEngine = {
    val engine : SSLEngine = TlsConfigUtil.createSslContext(keystoreFile, keystorePassword, truststoreFile, truststorePassword).createSSLEngine()
    engine.setNeedClientAuth(true)
    engine.setUseClientMode(false)
    engine
  }

  /**
    *
    * @return
    */
  def createTlsEngine(keystoreFile: File, keystorePassword: String,
                      truststoreFile: File, truststorePassword: String) : Engine = new Engine(createSslEngine(keystoreFile,keystorePassword,truststoreFile,truststorePassword))

}
