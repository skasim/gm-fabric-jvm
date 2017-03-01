package com.deciphernow.server.security

/**
 * A manager that determines if a client is authorized or not
 */
trait SimpleAccessManager {
  def isAuthorized(client: String): Boolean
  def isAuthorized(client: String, userToken: String): Boolean
}
