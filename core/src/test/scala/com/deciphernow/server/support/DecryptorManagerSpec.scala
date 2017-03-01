package com.deciphernow.server.support

import org.scalatest.{Matchers, FlatSpec}

/**
  * Created by ghershfield on 6/8/16.
  */
class DecryptorManagerSpec extends FlatSpec with Matchers {

  /**
    * Default to the Default decrypter : NoDecrypter
    */
  "Given a unencrypted resource string" should " return the same string" in {
    val instance = DecryptorManager.getInstance
    val resource = "password"
    instance.decryptResource(resource) should equal(resource)
  }
  "Given an encrypted resource string" should " return the unencrypted string" in {
    val encryptedResource = "-->{706c75525a30322f5a615a3846363776474764622f7861762f75334a593551682f445237386964676a52513d}"
    val decryptedResource = "ADecryptedString"
    val instance = DecryptorManagerTester.getInstance
    instance.decryptResource(encryptedResource) should equal(decryptedResource)
  }
}

object DecryptorManagerTester extends DecryptorManagerTester

class DecryptorManagerTester extends DecryptorManager {
  override lazy val className : Option[String] = Option("com.deciphernow.server.support.FakeDecryptor")
}
