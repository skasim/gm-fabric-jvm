package com.deciphernow.server

import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Request => FinagleRequest, Response => FinagleResponse}
import com.twitter.finatra.http.Controller

/**
  *
  * @param filters
  * @param controllers
  */
case class RestServer(filters: Seq[Filter[FinagleRequest, FinagleResponse,FinagleRequest, FinagleResponse]],controllers: Seq[Controller]) {
  def this(controllers: Seq[Controller]) = this(Nil,controllers)
  def this(filters: Seq[Filter[FinagleRequest, FinagleResponse,FinagleRequest, FinagleResponse]],controller: Controller) = this(filters,controller :: Nil)
  def this(controller: Controller) = this(controller :: Nil)
}
