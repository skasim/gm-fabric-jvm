package com.deciphernow.server.security

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
