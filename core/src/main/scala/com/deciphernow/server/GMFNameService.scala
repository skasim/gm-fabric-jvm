package com.deciphernow.server

/**
  * Copyright 2017 Decipher Technology Studios LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import java.net.{InetAddress, UnknownHostException}

import com.google.common.net.InetAddresses

import sun.net.spi.nameservice.NameService

/**
  * Provides an implementation of the [[NameService]] interface that prevents DNS lookups for a single IP address.
  */
object GMFNameService extends NameService {

  val string = sys.env.getOrElse("GMF_IP_ADDRESS", "0.0.0.0")
  val address = InetAddresses.forString(string)

  /**
    * Returns the addresses that resolve for a hostname.
    *
    * @param host the hostname
    * @return the addresses
    * @throws UnknownHostException if the hostname does not resolve
    */
  override def lookupAllHostAddr(host: String): Array[InetAddress] = throw new UnknownHostException(host)

  /**
    * Returns the hostname that corresponds to an address.
    *
    * @param bytes the address
    * @return the hostname
    * @throws UnknownHostException if the address does not correspond to a hostname
    */
  override def getHostByAddr(bytes: Array[Byte]): String = {
    if (bytes.sameElements(address.getAddress)) string else throw new UnknownHostException()
  }

}