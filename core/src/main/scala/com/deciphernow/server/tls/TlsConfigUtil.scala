package com.deciphernow.server.tls

import java.io.{File, FileInputStream, InputStream}
import java.security.KeyStore
import javax.net.ssl.{KeyManagerFactory, SSLContext, SSLEngine, TrustManagerFactory}

import com.deciphernow.server.support.{Decryptor, DecryptorManager}
import com.deciphernow.server.tls.ClientAuthorization._
import scala.util.Try


object TlsConfigUtil {

  /**
    *
    * @param sslContext the context for the SSL Engine
    * @param clientAuth which mode to use for client authentication
    * @return
    */
  def mkServer(sslContext: SSLContext, clientAuth: ClientAuthorization) : SSLEngine = {
    val engine = sslContext.createSSLEngine()
    engine.setUseClientMode(false)
    clientAuth match {
      case WANT => engine.setWantClientAuth(true)
      case NEED => engine.setNeedClientAuth(true)
      case _ =>
    }
    engine
  }

  /**
    *
    * @param sslContext
    * @return
    */
  def mkClient(sslContext: SSLContext) : SSLEngine = {
    val engine = sslContext.createSSLEngine()
    engine.setUseClientMode(true)
    engine.setNeedClientAuth(false)
    sslContext.createSSLEngine()
    engine
  }

  /**
   * Create an SSL context for use in an SSL Engine
   *
   * @param keyStoreFile the file representing the private keystore
   * @param keyStorePassword the password to the keystore
   * @param trustStoreFile the file representing the trust store
   * @param trustStorePassword the password to the trust store
   * @return an SSLContext suitable for creating the server or client TLS engine
   */
  def createSslContext(keyStoreFile: File,
                       keyStorePassword: String,
                       trustStoreFile: File,
                       trustStorePassword: String): SSLContext = {

    val (ksIs, tsIs) = (new FileInputStream(keyStoreFile), new FileInputStream(trustStoreFile))
    try {
      createSslContext(ksIs, keyStorePassword, tsIs, trustStorePassword)
    } finally {
      Try(ksIs.close())
      Try(tsIs.close())
    }
  }

  /**
   * Create an SSL context for use in an SSL Engine.  Note that this method does not close your input streams!
   *
   * @param keyStoreStream the input stream representing the private keystore
   * @param keyStorePassword the password to the keystore
   * @param trustStoreStream the input stream representing the trust store
   * @param trustStorePassword the password to the trust store
   * @return an SSLContext suitable for creating the server or client TLS engine
   */
  def createSslContext(keyStoreStream: InputStream,
                       keyStorePassword: String,
                       trustStoreStream: InputStream,
                       trustStorePassword: String): SSLContext = {

    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    val trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
    val trustStore = KeyStore.getInstance(KeyStore.getDefaultType)

    val decryptedKeystorePassword = decrypter.decryptResource(keyStorePassword).toCharArray()
    val decryptedTrustorePassword = decrypter.decryptResource(trustStorePassword).toCharArray()

    keyStore.load(keyStoreStream, decryptedKeystorePassword)
    keyManagerFactory.init(keyStore, decryptedKeystorePassword)
    trustStore.load(trustStoreStream, decryptedTrustorePassword)
    trustManagerFactory.init(trustStore)

    val sslContext = SSLContext.getInstance("TLS")

    sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, null)
    sslContext
  }

  lazy val decrypter : Decryptor = DecryptorManager.getInstance

}
