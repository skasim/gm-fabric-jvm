package com.deciphernow.server.security

import com.deciphernow.server.tls.X509CertificateHelper
import com.twitter.finagle._

/**
  * Created by ghershfield
  *
  * @param self
  * @tparam Req
  * @tparam Rep
  */
class ClientCertificateServiceFactory[Req, Rep](self: ServiceFactory[Req, Rep]) extends ServiceFactoryProxy[Req, Rep](self) {
  def this(service: Service[Req, Rep]) = this(ServiceFactory.const(service))

  object X509CertificateTransformer extends X509CertificateHelper

  override def apply(conn: ClientConnection) = {
    val filter = new SimpleFilter[Req, Rep] {
      def apply(request: Req, service: Service[Req, Rep]) = {
        com.twitter.finagle.transport.Transport.peerCertificate.fold(UserAuthentication.set(None)){cert =>
          val subjectDN = X509CertificateTransformer.translate(cert.getEncoded).getSubjectDN
          UserAuthentication.set(Some(new UserAuthentication(subjectDN.getName)))
        }
        val resp = service(request)
        UserAuthentication.clear()
        resp
      }
    }

    self(conn) map {
      filter andThen _
    }
  }
}
