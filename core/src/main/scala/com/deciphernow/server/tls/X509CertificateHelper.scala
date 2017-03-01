package com.deciphernow.server.tls

import java.io.ByteArrayInputStream
import java.security.cert.{CertificateFactory, X509Certificate}

/**
  * Created by ghershfield on 5/20/16.
  */
class X509CertificateHelper {

  val x509Factory = CertificateFactory.getInstance("X.509");
  
  def translate(bytes: Array[Byte]) : X509Certificate = x509Factory.generateCertificate(new ByteArrayInputStream(bytes)).asInstanceOf[X509Certificate]

}
