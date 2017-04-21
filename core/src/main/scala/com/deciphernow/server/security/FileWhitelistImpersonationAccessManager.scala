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
