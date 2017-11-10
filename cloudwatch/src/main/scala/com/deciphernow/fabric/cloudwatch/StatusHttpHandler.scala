package com.deciphernow.fabric.cloudwatch

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

import com.twitter.finagle.http.{HttpMuxHandler, Request, Response, Status}
import com.twitter.util.Future
import com.twitter.io.Buf.Utf8

class StatusHttpHandler extends HttpMuxHandler {
  val pattern: String = "/admin/metrics/cloudwatch.json"
  val metricsPublisher = new MetricsPublisher()

  def apply(request: Request): Future[Response] = {
    val response = Response(request.version, Status.Ok)
    response.setContentTypeJson()
    response.content = Utf8.apply(metricsPublisher.status())
    Future.value(response)
  }
}
