package com.deciphernow.server.filters


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

import com.twitter.finagle.stats.LoadedStatsReceiver
import com.twitter.finagle.http.{Response => HttpResponse, Request => HttpRequest, Method}
import com.twitter.finagle.http.{ Response}
import com.twitter.finagle.stats.{ Counter, Stat, StatsReceiver }
import com.twitter.finagle.{ Service, SimpleFilter }
import com.twitter.finatra.http.contexts.RouteInfo
import com.twitter.logging.Logger
import com.twitter.util.{ Duration, Future, Memoize, Stopwatch }
import com.deciphernow.server.support.SupportUtils.{convertToMap, createArray}

/**
  * Assists creating new stats handlers.
  */
object GenericUriStatsFilter {
  object Stats {
    // Unless stats are per-endpoint, don't track request count/time
    // since those are handled by [[com.twitter.finagle.service.StatsFilter]]
    // already.
    def mk(statsReceiver: StatsReceiver, statusCode: Int, perEndpoint: Boolean): Stats = {
      val statusClass = s"${statusCode / 100}XX"
      Stats(
        requestCount = if (perEndpoint) Some(statsReceiver.counter("requests")) else None,
        statusCodeCount = statsReceiver.scope("status").counter(statusCode.toString),
        statusClassCount = statsReceiver.scope("status").counter(statusClass),
        requestTime = if (perEndpoint) Some(statsReceiver.stat("time")) else None,
        statusCodeTime = statsReceiver.scope("time").stat(statusCode.toString),
        statusClassTime = statsReceiver.scope("time").stat(statusClass),
        responseSize = statsReceiver.stat("response_size"))
    }
  }

   case class Stats(requestCount: Option[Counter],
                    statusCodeCount: Counter,
                    statusClassCount: Counter,
                    requestTime: Option[Stat],
                    statusCodeTime: Stat,
                    statusClassTime: Stat,
                    responseSize: Stat) {
    def count(duration: Duration, response: Response): Unit = {
      requestCount.foreach { _.incr() }
      statusCodeCount.incr()
      statusClassCount.incr()

      val durationMs = duration.inMilliseconds
      requestTime.foreach { _.add(durationMs.toFloat) }
      statusCodeTime.add(durationMs.toFloat)
      statusClassTime.add(durationMs.toFloat)

      responseSize.add(response.length.toFloat)
    }
  }
}

/**
  * This filter should be used when URL's in a Controller are generic and one wants to individually capture
  * the metrics per dynamic url.
  *
  * For example: get("/:*") then you would want to capture the dynamic matches.
  *
  * You can explicitly block static URL's so that 2 StatsReceiver are not created.
  * Use the property: -com.deciphernow.server.config.staticRoutes.ignore=/ping,/bonkers ... etc
  *
  * Always start with the '/'
  *
  * Created by ghershfield on 8/3/16.
  */
class GenericUriStatsFilter extends SimpleFilter[HttpRequest,HttpResponse] {

  import com.deciphernow.server.filters.GenericUriStatsFilter.Stats

  private val statsReceiver: StatsReceiver = LoadedStatsReceiver

  val log = Logger.get(getClass)
  val notGenericUri = convertToMap(createArray(Option(com.deciphernow.server.config.staticRoutes.ignore.apply)))

  override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
    if (!notGenericUri.contains(request.uri)) {
      val uriOfInterest = if (request.uri.startsWith("/")) {
        request.uri.substring(1) // remove the start '/'
      }
      else {
        request.uri
      }

      // drop the attributes.
      val cleanUrl = if (uriOfInterest.contains("?")) {
        uriOfInterest.substring(0,uriOfInterest.indexOf("?"))
      }
      else {
        uriOfInterest
      }

      //val routeInfo = new RouteInfo(uriOfInterest, request.uri)
      val routeInfo = new RouteInfo(cleanUrl, cleanUrl)
      val elapsed = Stopwatch.start()
      service(request) flatMap { resp =>
        val elapsedTime = elapsed()
        log.ifDebug("The route is generic ... creating it's own statsReceiver: ["+routeInfo.path+"]")
        globalStats(resp.statusCode).count(elapsedTime, resp)
        perRouteStats((routeInfo, request.method, resp.statusCode)).count(elapsedTime, resp)
        Future(resp)
      }
    }
    else {
      log.ifTrace("THE FOLLOWING ROUTE IS STATIC: [" + request.uri + "]")
      service(request)
    }
  }

  private val globalStats = Memoize[Int, Stats] { statusCode =>
    Stats.mk(statsReceiver, statusCode, perEndpoint = false)
  }

  private val perRouteStats = Memoize[(RouteInfo, Method, Int), Stats] {
    case (routeInfo, method, statusCode) =>

      val nameOrPath =
        if (routeInfo.name.nonEmpty) { routeInfo.name }
        else { routeInfo.sanitizedPath }

      val scopedStatsReceiver =
        statsReceiver.
          scope("route").
          scope(nameOrPath).
          scope(method.toString.toUpperCase)
      Stats.mk(scopedStatsReceiver, statusCode, perEndpoint = true)

  }
}
