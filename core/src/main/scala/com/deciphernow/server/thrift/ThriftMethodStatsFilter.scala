package com.deciphernow.server.thrift

import java.util.concurrent.TimeUnit

import com.deciphernow.server.{config => configuration}
import com.twitter.finagle.stats.{LoadedStatsReceiver, Stat, StatsReceiver}
import com.twitter.finagle.thrift.InputBuffers
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.logging.Logger
import com.twitter.util.Future
import org.apache.thrift.protocol.TProtocolFactory
import org.apache.thrift.transport.TMemoryInputTransport

/**
  * Created by ghershfield on 5/17/16.
  *
  * This class tracks method level statistics for the thrift protocol.
  *
  * @param protocolFactory
  * @param serviceName
  * @param servicePath
  */
class ThriftMethodStatsFilter(protocolFactory: TProtocolFactory, serviceName: String, servicePath: String)
  extends SimpleFilter[Array[Byte], Array[Byte]] {

  val log = Logger.get(getClass)
  private val statsReceiver: StatsReceiver = LoadedStatsReceiver

  override def apply(request: Array[Byte], service: Service[Array[Byte], Array[Byte]]): Future[Array[Byte]] = {

    val buffer = new InputBuffer(request, protocolFactory)
    val msg = buffer().readMessageBegin()
    val timeValue = configuration.stats.time.get.fold("")(_ => configuration.stats.time.get.get)
    val timeUnit = timeMap.getOrElse(timeValue.toLowerCase,TimeUnit.MILLISECONDS)
    log.ifDebug("timeUnit in ==> " + timeUnit)
    val state = Stat.timeFuture(statsReceiver.stat(s"${serviceName}${servicePath}${msg.name}"), timeUnit) { service(request) }
    state.onFailure{
      case ex => statsReceiver.counter(s"${serviceName}${servicePath}${msg.name}/exception/${ex.getClass.getName}").incr()
    }
  }

  val timeMap = Map(
    "nanoseconds" -> TimeUnit.NANOSECONDS,
    "microseconds" -> TimeUnit.MICROSECONDS,
    "milliseconds" -> TimeUnit.MILLISECONDS,
    "seconds" -> TimeUnit.SECONDS,
    "minutes" -> TimeUnit.MINUTES,
    "hours" -> TimeUnit.HOURS,
    "days" -> TimeUnit.DAYS
  )
}

/**
  * Copy of com.twitter.finagle.thrift.InputBuffer (It's private :()
  *
  *
  * This is Twitter code and the private modifier has been removed. This
  * piece of code is covered under licensing that Twitter used
  * as of 2016-06-01. At the time it is Apache 2.0 License.
  *
  * @param bytes
  * @param protocolFactory
  */
private class InputBuffer(bytes: Array[Byte], protocolFactory: TProtocolFactory) {

  private[this] val memoryTransport = new TMemoryInputTransport(bytes)
  private[this] val iprot = protocolFactory.getProtocol(memoryTransport)

  def apply() = iprot

  def remainder = {
    val length = bytes.length
    memoryTransport.getBufferPosition match {
      case 0 => bytes
      case l if l == length => InputBuffers.EmptyBytes
      case position => {
        val diff = length - position
        val newBytes = new Array[Byte](diff)
        System.arraycopy(bytes, position, newBytes, 0, diff)
        newBytes
      }
    }
  }
}
