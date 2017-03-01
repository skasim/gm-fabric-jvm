package com.deciphernow.server.tls

/**
 * Enum for custom SSL engine configuration.  Determines if the engine should ask for and require mutual auth
 */
object ClientAuthorization extends Enumeration {
  type ClientAuthorization = Value
  val WANT, NEED, NONE = Value
}
