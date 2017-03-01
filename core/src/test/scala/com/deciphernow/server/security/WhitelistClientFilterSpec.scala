package com.deciphernow.server.security

import com.twitter.finagle.Service
import com.twitter.util.{Await, Future}
import org.scalatest._

/**
 * Created with IntelliJ IDEA.
 * User: bpfoster
 * Date: 1/27/15
 * Time: 11:52 AM
 */
class WhitelistClientFilterSpec extends FlatSpec with Matchers {

  val accessManager = new SimpleAccessManager {
    override def isAuthorized(client: String): Boolean = client == "ok"
    override def isAuthorized(client: String, userToken: String): Boolean = client == "ok"
  }

  val service = new Service[String, String] {
    def apply(req: String) = {
      Future.value(req)
    }
  }

  val filter: WhitelistClientFilter[String, String] = new WhitelistClientFilter[String, String](accessManager = accessManager)


  "A WhitelistClientFilter" should "deny when no user is present" in {
    val retVal: Future[String] = filter.apply("in", service)
    a[UnauthorizedException] should be thrownBy {
      Await.result(retVal)
    }
  }

  it should "deny when the user is not authorized" in {
    val retVal: Future[String] = UserAuthentication("bad", None).asCurrent {
      filter.apply("in", service)
    }

    a[UnauthorizedException] should be thrownBy {
      Await.result(retVal)
    }
  }

  it should "allow when the user is authorized" in {
    val retVal: Future[String] = UserAuthentication("ok", None).asCurrent {
      filter.apply("in", service)
    }
    Await.result(retVal) should equal("in")
  }

}
