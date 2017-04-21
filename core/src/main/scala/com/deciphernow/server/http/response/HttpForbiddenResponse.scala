package com.deciphernow.server.http.response


/*
    Copyright 2017 Decipher Technology Studios LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import org.jboss.netty.handler.codec.http
import org.jboss.netty.handler.codec.http.{HttpVersion, DefaultHttpResponse, HttpResponseStatus}

/**
  * Created by ghershfield on 5/31/16.
  */
class HttpForbiddenResponse extends com.twitter.finagle.http.Response {
  override def httpResponse: http.HttpResponse = {
    val hrs = new HttpResponseStatus(403,"No HTTP requests allowed.")
    new DefaultHttpResponse(HttpVersion.HTTP_1_1,hrs)
  }
}
