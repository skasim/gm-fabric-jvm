package com.deciphernow.server.client


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

import java.util.concurrent.Executors
import javax.net.ssl.SSLContext

import com.deciphernow.server.config.client.{numWorkers, useSeparatePool}
import com.twitter.concurrent.NamedPoolThreadFactory
import com.twitter.conversions.time._
import com.twitter.finagle.Thrift
import com.twitter.finagle.client.{DefaultPool, StackClient}
import com.twitter.finagle.netty3.Netty3Transporter.ChannelFactory
import com.twitter.finagle.service.FailFastFactory.FailFast
import com.twitter.finagle.service.{Retries, RetryBudget}
import com.twitter.finagle.ssl.Engine
import com.twitter.finagle.thrift.ClientId
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Duration, Stopwatch}
import org.jboss.netty.channel.socket.nio.{NioClientSocketChannelFactory, NioWorkerPool}

/**
  *
  */
object ThriftClientFactory {

  lazy val executor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(name= "finagle/clients", makeDaemons = true))
  lazy val clientSocketChannelFactory = new NioClientSocketChannelFactory(executor, 1, new NioWorkerPool(executor, numWorkers())) {
    override def releaseExternalResources(): Unit = () // no-op; unreleasable
  }

  implicit def boolean2Option(b: Boolean): Option[Boolean] = {
    if (b) { Some(b) }
    else { None }
  }
}

/**
  *
  */
class ThriftClientFactory {

  import com.deciphernow.server.client.ThriftClientFactory._

  protected val failFast = true
  protected val numRetries = 3
  protected val hostConnectionLimit = Int.MaxValue
  protected val timeout = Duration.fromSeconds(20)
  protected val sslHostVerify = None

  protected def newBudget(): RetryBudget =
    RetryBudget(
      ttl = 1.second,
      minRetriesPerSec = 3,
      percentCanRetry = 0.0,
      nowMillis = Stopwatch.timeMillis)

  protected val defaultClientParams = StackClient.defaultParams +
    FailFast(failFast) +
    Retries.Budget(newBudget()) +
    DefaultPool.Param.param.default.copy(high = hostConnectionLimit) +
    useSeparatePool().map(_=> ChannelFactory(clientSocketChannelFactory)).getOrElse(ChannelFactory.param.default)


  /**
    *
    * @param serviceName
    * @param sslContext
    * @return
    */
  protected def newConfiguredClient(serviceName: String, sslContext: SSLContext) = {

    var fullParams = defaultClientParams

    if (serviceName != null) {
      fullParams = fullParams + Thrift.param.ClientId(Option(new ClientId(serviceName)))
    }

    if (sslContext == null) {
      Thrift.client.withLabel(serviceName).withParams(fullParams)
    }
    else {
      Thrift.client.withLabel(serviceName)
        .withParams(fullParams)
        .configured(Transport.TLSClientEngine(Option(socketAddress => {
          val engine = sslContext.createSSLEngine()
          engine.setUseClientMode(true)
          engine.setNeedClientAuth(false)
          new Engine(engine)
      })))
    }
  }
}

