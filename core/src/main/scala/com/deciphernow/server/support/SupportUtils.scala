package com.deciphernow.server.support

import com.deciphernow.server.{config => configuration}

/**
  * Created by ghershfield on 6/24/16.
  */
object SupportUtils {

  /**
    * Create the thrift label to identify if the thrift service is non-ssl or 2-way ssl.
    *
    * @param sslState
    * @return
    */
    def createServerLabel(prefix: String, sslState : Boolean) : String = {
      sslState match {
        case true  => prefix + "::ssl"
        case false => prefix + "::non-ssl"
      }
    }

  /**
    * Have both jks required for 2-way SSL been provided?
    *
    * @return
    */
  def isSsl : Boolean = !(configuration.tls.keyStore().isEmpty || configuration.tls.trustStore().isEmpty)

  /**
    * Convert an array of strings to a map. The key/value pair have the same value.
    *
    * @param data
    * @return
    */
  def convertToMap(data: Array[String]) : collection.mutable.Map[String,String] = {
    val map = collection.mutable.Map[String, String]()
    for(e <- data) {
      if (!e.trim.equalsIgnoreCase("")) {
        map put(e.trim, e.trim)
      }
    }
    map
  }

  /**
    * Create an array of String when given a comma separated String.
    * @param attribute
    * @return
    */
  def createArray(attribute: Option[String]) : Array[String] = attribute.fold(Array[String]())(_ => attribute.get.split(","))

}
