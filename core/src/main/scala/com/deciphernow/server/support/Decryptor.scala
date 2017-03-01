package com.deciphernow.server.support

/**
  * Created by ghershfield on 6/8/16.
  */
trait Decryptor {
  /**
    *
    * @param string
    * @return
    */
  def decryptResource(string: String) : String
}
