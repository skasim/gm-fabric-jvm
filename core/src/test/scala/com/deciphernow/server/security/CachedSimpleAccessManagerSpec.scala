package com.deciphernow.server.security

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
