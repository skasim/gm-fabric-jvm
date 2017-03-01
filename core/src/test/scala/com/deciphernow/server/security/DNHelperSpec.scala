package com.deciphernow.server.security

import org.scalatest.{FlatSpec,Matchers}

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
