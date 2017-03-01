package com.deciphernow.server.security

/**
 * Created by ghershfield on 10/1/15.
 */
class DNHelper {


  def normalizeDistinguishedNameOption(dn:Option[String]) : Option[String] = dn.map(dn => normalizeDistinguishedName(dn))

  def normalizeDistinguishedName(dn:String) : String = {
    if (dn=="") {
      dn
    }
    else {

      val replaced = dn.replace("/", ",")
      val splitOut = replaced.split(",")
      val validCount = getCount(splitOut)
      val trimmed = trim(splitOut, validCount)

      // Don't have to worry about case since 'trim' toLowers as it trims.
      val newDN = if (trimmed(0).startsWith("cn")) {
        trimmed.mkString(",")
      }
      else {
        val tmp = trimmed.reverse
        tmp.mkString(",")
      }
      newDN
    }
  }

  protected[this] def trim(v:Array[String],max:Int) : Array[String] = {
    if (max > 0) { v.filter(elem => elem.length > 0).map(elem => elem.trim.toLowerCase) }
    else { v }
  }

  protected[this] def getCount(v:Array[String]) : Int = v.filter(elem => elem.trim.length > 0).size

}
