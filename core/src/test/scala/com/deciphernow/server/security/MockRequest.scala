package com.deciphernow.server.security


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

import java.net.InetSocketAddress

import com.twitter.finagle.http.{Method, Request, Response, Version}
import org.jboss.netty.handler.codec.http._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MockRequest extends Request {
   val httpRequest: HttpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
   override val httpMessage: HttpMessage = httpRequest
   val remoteSocketAddress = new InetSocketAddress("127.0.0.1", 12345)

   def getMockHttpRequest() = httpRequest

   // Create a MockRequest with a specific IP
   def withIp(ip: String) =
     new MockRequest {
       override val httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
       override final val httpMessage = httpRequest
       override val remoteSocketAddress = new InetSocketAddress(ip, 12345)
     }

   // Create an internal MockRequest
   def internal = withIp("10.0.0.1")

   // Create an external MockRequest
   def external = withIp("8.8.8.8")
 }