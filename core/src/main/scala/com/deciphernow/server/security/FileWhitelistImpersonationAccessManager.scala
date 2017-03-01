package com.deciphernow.server.security

import java.io.File

import scala.concurrent.duration.{Duration, _}
import scala.io.Source


/**
 * A caching ImpersonationAccessManager that reads its whitelist from a file
 *
 * @param whitelistFile the file that represents the whitelist
 * @param reloadTime how long before reloading the list.
 */
class FileWhitelistImpersonationAccessManager(whitelistFile: File, reloadTime: Duration = 10 minutes)
  extends CachedSimpleAccessManager(reloadTime) {

  if (!whitelistFile.canRead) {
    throw new IllegalArgumentException(s"Whitelist file ${whitelistFile} cannot be read!")
  }

  val dnHelper = new DNHelper
  override def loadWhitelist(): Set[String] = {
    /*
      The correct manner of handling the white list is to allow for any of the
      tokens to be entered incorrectly and to normalize and then convert to
      uppercase the information for comparision.
    */
    val tmp = Source.fromFile(whitelistFile, "UTF-8").getLines().toSet
    val newSet = tmp.map(v => dnHelper.normalizeDistinguishedName(v.toLowerCase))
    newSet
  }
}
