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

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder
import com.amazonaws.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest}
import com.twitter.finagle.stats.MetricsStatsReceiver
import java.lang.management.ManagementFactory
import java.util.Date
import java.util.concurrent.{Executors, TimeUnit}
import javax.management.ObjectName

import org.slf4j.LoggerFactory

import scala.sys.process._
import scala.util.parsing.json.JSONObject
import scala.collection.JavaConverters._

trait Logging {
  protected val logger = LoggerFactory.getLogger(classOf[MetricsPublisher])
}

trait FlagInitializationScheduler extends Logging {
  this: MetricsPublisher =>

  private val checkingFlagInitializationScheduler = Executors.newScheduledThreadPool(1)
  private val checkFlagInitialization = new Runnable() {
    def run() = {
      if (metricNames().isEmpty) {
        logger.warn("There are no metrics configured to be published! (did you forget to configure metricsNames?)")
      } else {
        checkingFlagInitializationScheduler.shutdown()
        logger.info(s"Publishing metrics every: ${period()} seconds")
        publishingMetricsScheduler.scheduleAtFixedRate(publishMetrics, 0, period(), TimeUnit.SECONDS)
      }
    }
  }
  checkingFlagInitializationScheduler.scheduleAtFixedRate(checkFlagInitialization, 5, 5, TimeUnit.SECONDS)

  private[cloudwatch] val publishingMetricsScheduler = Executors.newScheduledThreadPool(1)
  private[cloudwatch] val publishMetrics = new Runnable() {
    def run() = {
      try {
        logger.debug(
          s"""Publishing metrics to CloudWatch:
             |Region: ${region()}
             |Namespace: ${namespace()}
             |Period: ${period()}
             |Metric Names: ${metricNames()}
             |Service Name: ${serviceName()}
           """.stripMargin)

        val client = createClient

        val requestData = metricNames().flatMap { key =>
          makeMetricDatum(key)
        }.toSeq

        val request = new PutMetricDataRequest()
          .withNamespace(namespace())
          .withMetricData(requestData.asJavaCollection)

        client.putMetricData(request)
        lastPublished = new Date()
      } catch {
        case e: Exception => logger.error("An exception occurred while publishing metrics:", e)
      }
    }

  }
}

/**
  *
  */
class MetricsPublisher extends FlagInitializationScheduler {

  import MetricsPublisher._

  private[cloudwatch] implicit var lastPublished = new Date()

  // get sample metrics from the registry
  private[cloudwatch] def sample = MetricsStatsReceiver
    .defaultRegistry
    .sample
    .asScala
    .toMap

  // create a publishing client
  private[cloudwatch] def createClient = AmazonCloudWatchClientBuilder
    .standard()
    .withCredentials(new DefaultAWSCredentialsProviderChain())
    .withRegion(region())
    .build()

  private def maybeMetric(key: String): Option[MetricDatum] = sample.find(a => a._1 == key) match {
    case None => None
    case Some(d) => Some(Metric(d, lastPublished).asDatum)
  }


  /**
    * Make the appropriate type of metricdatum
    * @param key
    * @return
    */
  def makeMetricDatum(key: String): Option[MetricDatum] = key match {
    case `processCpuPercent` => maybeCpu
    case `processMemoryKb` => maybeMemory.headOption
    case `processMemoryPercent` => maybeMemory.lastOption
    case _ => maybeMetric(key)
  }

  /**
    * Provides status function to statushttphandler
    * @return
    */
  def status(): String = JSONObject(Map(
    "period" -> period(),
    "namespace" -> namespace(),
    "metricNames" -> metricNames(),
    "serviceName" -> serviceName(),
    "lastPublished" -> lastPublished
  )).toString
}

/**
  *
  */
object MetricsPublisher extends Logging {

  val processCpuPercent = "process/cpu/percent"
  val processMemoryKb = "process/memory/kb"
  val processMemoryPercent = "process/memory/percent"

  // return cpu usage if available
  def maybeCpu(implicit lastPublished: Date): Option[MetricDatum] = ManagementFactory
    .getPlatformMBeanServer
    .getAttributes(ObjectName.getInstance("java.lang:type=OperatingSystem"), Array("ProcessCpuLoad"))
    .asList()
    .asScala
    .headOption
    .map { v => Metric(processCpuPercent, (v.getValue.asInstanceOf[Double] * 1000).toInt / 10.0, lastPublished).asDatum}
    .filter(a => a.getValue != -1.0)


  /**
    * obtain memory usage if available
    * @param lastPublished
    * @return
    */
  def maybeMemory(implicit lastPublished: Date) : Seq[MetricDatum] = {
    def md(key: String, value: Double) = Metric(key, value, lastPublished).asDatum
    val heapUsage = ManagementFactory.getMemoryMXBean.getHeapMemoryUsage;
    val max = Double.box(heapUsage.getMax).doubleValue / 1024
    val used = Double.box(heapUsage.getUsed).doubleValue / 1024
    logger.debug("processMemoryKb :: {} Percent Used :: {} ",used,(used / max))
    Seq(md(processMemoryKb,used), md(processMemoryPercent,(used / max)))
  }


}
