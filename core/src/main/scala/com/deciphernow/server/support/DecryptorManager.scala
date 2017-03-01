package com.deciphernow.server.support

/**
  * Created by ghershfield on 6/8/16.
  */
object DecryptorManager extends DecryptorManager

class DecryptorManager {

  lazy val className : Option[String] = com.deciphernow.server.config.resources.decryptClass.getWithDefault
  lazy val clazz: Class[_] = Class.forName(className.get)
  lazy val instance = clazz.newInstance().asInstanceOf[Decryptor]

  def getInstance : Decryptor = instance

}
