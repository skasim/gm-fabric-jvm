package com.deciphernow.server

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

import java.net.InetSocketAddress

import com.twitter.finagle.Announcer
import com.deciphernow.server.{config => configuration}
import com.google.common.net.InetAddresses
import com.twitter.logging.Logger

/**
  *
  */
object GMFAnnouncer {

  val log = Logger.get(getClass)

  /**
    * Registers endpoint with ZK.
    * @param port
    * @param scheme
    */
  def announce(port : String, scheme: String) : Unit = {
    val tmp = if (port.startsWith(":")) port.substring(1) else port

    if (tmp.trim.length>0) {
      try {
        announce(tmp.toInt,scheme)
      }
      catch { case e : Exception => log.ifInfo(e) }
    }
  }

  /**
    * Registers endpoint with ZK.
    * @param port
    * @param scheme
    */
  def announce(port : Int, scheme: String) : Unit = {
    val host = GMFNetworkConfigurationResolver.getAnnounceHostname
    val prefix = if (InetAddresses.isInetAddress(host)) "gm" else "zk"
    val announcementPoint = s"${prefix}!${configuration.zk.zookeeperConnection()}!${configuration.zk.announcementPoint()}/${scheme}!0"
    Announcer.announce(new InetSocketAddress(GMFNetworkConfigurationResolver.getAnnounceHostname, port), announcementPoint)
  }
}
