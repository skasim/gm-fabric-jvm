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
