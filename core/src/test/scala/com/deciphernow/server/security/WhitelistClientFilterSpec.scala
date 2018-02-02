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

import com.twitter.finagle.Service
import com.twitter.util.{Await, Future}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Created with IntelliJ IDEA.
 * User: bpfoster
 * Date: 1/27/15
 * Time: 11:52 AM
 */
@RunWith(classOf[JUnitRunner])
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
