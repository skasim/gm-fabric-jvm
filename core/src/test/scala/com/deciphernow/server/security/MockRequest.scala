package com.deciphernow.server.security

import java.net.InetSocketAddress

import com.twitter.finagle.http.{Method, Request, Response, Version}
import org.jboss.netty.handler.codec.http._



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