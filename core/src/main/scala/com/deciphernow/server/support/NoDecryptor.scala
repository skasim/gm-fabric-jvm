package com.deciphernow.server.support

/**
  * Created by ghershfield on 6/8/16.
  *
  * When no resources are encrypted then this decrypter will be instantiated.
  *
  */
class NoDecryptor extends Decryptor {
  override def decryptResource(string: String): String = string
}
