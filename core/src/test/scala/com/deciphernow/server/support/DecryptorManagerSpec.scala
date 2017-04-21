package com.deciphernow.server.support


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
