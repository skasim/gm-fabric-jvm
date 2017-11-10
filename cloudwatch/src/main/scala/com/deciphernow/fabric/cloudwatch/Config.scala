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

import java.util.Date

import com.amazonaws.services.cloudwatch.model.{Dimension, MetricDatum, StandardUnit}
import com.twitter.app.GlobalFlag

object period extends GlobalFlag[Int](300, "The publish period in seconds. Metrics will be published every <period> seconds.")

object region extends GlobalFlag[String]("us-east-1", "The AWS region to send metrics to.")

object namespace extends GlobalFlag[String]("microservice", "The namespace that metrics are published under.")

object metricNames extends GlobalFlag[Set[String]](Set("process/cpu/percent","process/memory/mb","process/memory/percent","srv/load","srv/request_latency_ms.p90"), "The names of the metrics to publish.")

object pidName extends GlobalFlag[String]("wrapper.java.pid", "The name of the java process running on the instance")

object serviceName extends GlobalFlag[String]("not-configured","Should bee the name of the service metrics are being captured.")

/**
  * Represents a metric: key, value, date
  * wraps a convenience function to convert the simple metric to a MetricDatum
  * @param key
  * @param value
  * @param now
  */
case class Metric(key: String, value: Double, now: Date) {

  def asDatum: MetricDatum = new MetricDatum()
    .withMetricName(key)
    .withUnit(StandardUnit.None)
    .withTimestamp(now)
    .withValue(value)
    .withDimensions(
      new Dimension()
        .withName("Service Name")
        .withValue(serviceName())
    )
}
// Alternative means of creating a metric
object Metric {
  def apply(tuple: (String, Number), date: Date) = new Metric(tuple._1, tuple._2.doubleValue(), date)
  def apply(key: String, value: String, date: Date) = new Metric(key, value.toDouble, date)
}
