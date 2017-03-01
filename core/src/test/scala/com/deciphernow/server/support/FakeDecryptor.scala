package com.deciphernow.server.support

/**
  * Created by ghershfield on 6/20/16.
  */
class FakeDecryptor extends Decryptor {
  /**
    *
    * @param string
    * @return
    */
  override def decryptResource(string: String): String = {
    if (string.startsWith("-->{")) {
      "ADecryptedString"
    }
    else {
      string
    }
  }
}
