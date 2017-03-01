package com.deciphernow.server.support

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by ghershfield on 6/8/16.
  */
class NoDecryptorSpec extends FlatSpec with Matchers {

  "Given a string" should " return the same string" in {
    val decrypter = new NoDecryptor
    val password = "bonkers"
    decrypter.decryptResource(password) should equal(password)
  }

}
