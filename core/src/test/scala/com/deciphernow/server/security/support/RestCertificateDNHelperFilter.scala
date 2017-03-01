package com.deciphernow.server.security.support

import com.deciphernow.server.security.UserAuthentication
import com.twitter.finagle.http.{Request => HttpRequest, Response => HttpResponse}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

/**
  * Created by ghershfield on 6/2/16.
  */
class RestCertificateDNHelperFilter extends SimpleFilter[HttpRequest,HttpResponse] {

    override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
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
