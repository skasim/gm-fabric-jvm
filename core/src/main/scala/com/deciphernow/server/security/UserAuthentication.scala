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
