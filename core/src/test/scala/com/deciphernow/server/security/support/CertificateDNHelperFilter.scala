package com.deciphernow.server.security.support

import com.deciphernow.server.security.UserAuthentication
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future


/**
  * This class is used to acquire the DN from UserAuthentication allowing one
  * to validate it.
  *
  * Created by ghershfield on 6/1/16.
  */
class CertificateDNHelperFilter extends SimpleFilter[Array[Byte], Array[Byte]] {
  override def apply(request: Array[Byte], service: Service[Array[Byte], Array[Byte]]): Future[Array[Byte]] = {
    UserAuthentication.current match {
      case Some(_) => distinguishedName = Option(UserAuthentication.current.get.user)
      case None => distinguishedName = None
    }
    service(request)
  }

  var distinguishedName : Option[String] = None

  def getDistinguishedName() = distinguishedName
  def resetDistinguishedName = distinguishedName = None
}

