package com.deciphernow.server.security

import com.twitter.util.Local

case class UserAuthentication(user: String, authority: Option[String] = None) {
  def this(user: String, authority: String) = this(user, Some(authority))
  /**
   * Executes the given function with this UserAuthentication set as the current
   * UserAuthentication.  The current UserAuthentication before executing this will be restored
   * on completion.
   */
  def asCurrent[T](f: => T): T = {
    val old = UserAuthentication.current
    UserAuthentication.set(Some(this))
    try f finally { UserAuthentication.set(old) }
  }
}

/**
 * `UserAuthentication` provides the client identification of the incoming request if available.
 * It is set at the beginning of the request and is available throughout the life-cycle
 * of the request.
 */
object UserAuthentication {
  private[this] val _current = new Local[UserAuthentication]
  def current: Option[UserAuthentication] = _current()

  private[security] def set(clientId: Option[UserAuthentication]) = clientId.fold(_current.clear())(id => _current.update(id))

  private[security] def clear() = _current.clear()

}
