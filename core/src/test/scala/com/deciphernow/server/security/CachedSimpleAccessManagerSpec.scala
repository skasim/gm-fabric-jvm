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

import org.scalatest.{Matchers, FlatSpec}

import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: bpfoster
 * Date: 1/27/15
 * Time: 7:47 PM
 */
class CachedSimpleAccessManagerSpec extends FlatSpec with Matchers {
  val manager = new CachedSimpleAccessManager(2 seconds) {
    var whitelist: Set[String] = Set.empty

    override def loadWhitelist(): Set[String] = whitelist
  }


  "A CachedSimpleAccessManager" should "load and reload correctly" in {
    manager.whitelist = Set("foo")

    manager.isAuthorized("foo") should be equals(true)

    manager.whitelist = Set("foo", "bar")

    manager.isAuthorized("bar") should be equals(false)

    Thread.sleep((2 seconds).toMillis)

    manager.isAuthorized("bar") should be equals(true)
  }
}
