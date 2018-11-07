package com.deciphernow.server.rest

import com.twitter.finagle.{IndividualRequestTimeoutException}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.response.ResponseBuilder
import javax.inject.Inject

class IndividualRequestTimeoutExceptionMapper @Inject()(response: ResponseBuilder) extends ExceptionMapper[IndividualRequestTimeoutException] {

  override def toResponse(request: Request, throwable: IndividualRequestTimeoutException): Response = {
    response.status(504).body(s"Server is not able to get a response in time: ${throwable.getMessage}")
  }

}