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

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.logging.Logger
import com.twitter.util.Future

/**
 * Simple filter that checks the user attribute of UserAuthentication against the AccessManager.
 * If the user is not authorized, per the accessManager, an unauthorized exception is returned.
 *
 * @param accessManager the accessManager that determines if the user is authorized
 * @tparam Req
 * @tparam Rep
 */
class WhitelistClientFilter[Req, Rep](accessManager: SimpleAccessManager) extends SimpleFilter[Req, Rep] {

  val log = Logger.get(getClass)

  override def apply(request: Req, service: Service[Req, Rep]): Future[Rep] = {
    val maybeUserAuthentication: Option[UserAuthentication] = UserAuthentication.current
    val allowed = maybeUserAuthentication.map(ua => accessManager.isAuthorized(ua.user)).getOrElse(false)
    val message = maybeUserAuthentication.fold("")(ua => ua.user.toLowerCase)

    if (allowed) {
      log.ifDebug(s"Client ${message} is allowed")
      service(request)
    }
    else {
      log.ifDebug(s"Client ${message} is denied!")
      Future.exception(new UnauthorizedException(s"Client ${maybeUserAuthentication} is not authorized to access this service"))
    }
  }
}
