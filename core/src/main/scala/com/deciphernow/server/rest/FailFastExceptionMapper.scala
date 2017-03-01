package com.deciphernow.server.rest

import javax.inject.{Inject, Singleton}

import com.twitter.finagle.FailedFastException
import com.twitter.finagle.http.{Response, Request}
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.response.ResponseBuilder

/**
  * Created by ghershfield on 8/11/16.
  */
class FailFastExceptionMapper @Inject() (response: ResponseBuilder) extends ExceptionMapper[FailedFastException] {

  override def toResponse(request: Request, throwable: FailedFastException): Response = {
    response.badRequest(s"${throwable.getMessage}")
  }

}
