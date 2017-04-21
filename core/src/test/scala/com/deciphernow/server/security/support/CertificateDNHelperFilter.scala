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

