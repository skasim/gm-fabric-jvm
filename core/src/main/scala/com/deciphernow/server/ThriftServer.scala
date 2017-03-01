package com.deciphernow.server

import com.twitter.finagle.SimpleFilter

/**
  *
  * @param filters
  * @param service
  */
case class ThriftServer(filters: Seq[SimpleFilter[Array[Byte], Array[Byte]]], service: AnyRef) {
  def this(service: AnyRef) = this(Seq.empty, service)
}
