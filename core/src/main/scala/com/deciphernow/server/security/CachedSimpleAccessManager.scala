package com.deciphernow.server.security

import com.twitter.logging.Logger
import scala.concurrent.duration.Duration

/**
 * Abstract ImpersonationAccessManager that caches the whitelist for a given time.
 * Delegates loading of the actual list to the implementing class.
 *
 * @param reloadTime how long before reloading the list.
 */
abstract class CachedSimpleAccessManager(reloadTime: Duration) extends ImpersonationAccessManager
  with SimpleAccessManager {

  val log = Logger.get(getClass)
  private val reloadTimeMs = reloadTime.toMillis

  private var lastCheckTime = -1L
  private var whitelistedClients: Set[String] = _

  private val dnHelper = new DNHelper

  override def isAuthorized(client: String): Boolean = {
    val normalizedDN = dnHelper.normalizeDistinguishedName(client).toLowerCase
    val result = whitelist.contains(normalizedDN)
    if (result) {
      log.ifDebug(s"Client ${normalizedDN} is allowed")
    }
    else {
      log.ifDebug(s"Client ${normalizedDN} is denied!")
    }

    result
  }
  override def isAuthorized(client: String, userToken: String): Boolean = {
    var result = whitelist.contains(dnHelper.normalizeDistinguishedName(client).toLowerCase)

    val normalizedClient = dnHelper.normalizeDistinguishedName(client).toLowerCase
    val normalizedUserToken = dnHelper.normalizeDistinguishedName(userToken).toLowerCase

    if (result) {
      log.ifDebug(s"Client ${normalizedClient} is allowed it impersonate for ${normalizedUserToken}")
    }
    else {
      result = whitelist.contains(client)
      if (result) {
        log.ifDebug(s"Client ${normalizedClient} is allowed it impersonate for ${normalizedUserToken}")
      } else {
        log.ifDebug(s"Client ${normalizedClient} is denied! Unable to impersonate ${normalizedUserToken}")
      }
    }

    result
  }

  override def canImpersonateUser(clientId: String, user: String): Boolean = isAuthorized(clientId, user)

  private[this] def whitelist(): Set[String] = {
    val now = System.currentTimeMillis()
    if ((now - lastCheckTime) >= reloadTimeMs) {
      log.ifTrace("Whitelist age reached.  Reloading...")
      whitelistedClients = loadWhitelist()
      lastCheckTime = now
    }

    whitelistedClients
  }

  def loadWhitelist(): Set[String]
}
