package com.deciphernow.server.security

/**
 * A manager that determines whether a client has authorization to impersonate a user
 *
 */
trait ImpersonationAccessManager {
  def canImpersonateUser(clientId: String, user: String): Boolean
}
