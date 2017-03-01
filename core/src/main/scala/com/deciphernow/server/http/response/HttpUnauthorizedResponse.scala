package com.deciphernow.server.http.response

import org.jboss.netty.handler.codec.http
import org.jboss.netty.handler.codec.http.{HttpVersion, DefaultHttpResponse, HttpResponseStatus}

/**
  * Created by ghershfield on 5/31/16.
  */
class HttpUnauthorizedResponse(message: String) extends com.twitter.finagle.http.Response {
  override def httpResponse: http.HttpResponse = {
    val hrs = new HttpResponseStatus(401,message)
    new DefaultHttpResponse(HttpVersion.HTTP_1_1,hrs)
  }
}
