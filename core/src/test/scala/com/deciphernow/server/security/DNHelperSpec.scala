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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class DNHelperSpec extends FlatSpec with Matchers {

  val answer = "CN=Wombat Billy,OU=People,OU=Widgets,O=ACME Inc,C=US".toLowerCase
  val dnArray = Array(
    "",
    "C=US, O=ACME Inc, OU=Widgets, OU=People, CN=Wombat Billy",
    "CN=Wombat Billy,OU=People,OU=Widgets,O=ACME Inc,C=US",
    "CN=Wombat Billy,        OU=People,OU=Widgets,O=ACME Inc,C=US",
    "/C=US/O=ACME Inc/OU=Widgets/OU=People/CN=Wombat Billy",
    "",
    ""
  )

  "t1" should "work" in {
    val helperDN = new DNHelper
    var tmpVar = Option("")
    var tmpAnswer : Option[_] = None

    tmpAnswer = helperDN.normalizeDistinguishedNameOption(tmpVar)
    assert(tmpAnswer.get=="")
    tmpVar = Option("/C=US/O=ACME Inc/OU=Widgets/OU=People/CN=Wombat Billy")
    tmpAnswer = helperDN.normalizeDistinguishedNameOption(tmpVar)
    assert(answer==tmpAnswer.get.toString)
    tmpVar = None
    tmpAnswer = helperDN.normalizeDistinguishedNameOption(tmpVar)
    assert(tmpAnswer==None)
    tmpVar = Option("C=US, O=ACME Inc, OU=Widgets, OU=People, CN=Wombat Billy")
    tmpAnswer = helperDN.normalizeDistinguishedNameOption(tmpVar)
    assert(answer==tmpAnswer.get.toString)
    tmpVar = Option("CN=Wombat Billy,OU=People,OU=Widgets,O=ACME Inc,C=US")
    tmpAnswer = helperDN.normalizeDistinguishedNameOption(tmpVar)
    assert(answer==tmpAnswer.get.toString)
    tmpVar = Option("CN=Wombat Billy,        OU=People,OU=Widgets,O=ACME Inc,C=US")
    tmpAnswer = helperDN.normalizeDistinguishedNameOption(tmpVar)
    assert(answer==tmpAnswer.get.toString)
  }

  "bugWithSlashes" should "work" in {
    val helperDN = new DNHelper
    var sVar =helperDN.normalizeDistinguishedName("/C=US/O=ACME Inc/OU=Widgets/OU=People/CN=Wombat Billy")
    assert("cn=wombat billy,ou=people,ou=widgets,o=acme inc,c=us"==sVar)
  }

}
