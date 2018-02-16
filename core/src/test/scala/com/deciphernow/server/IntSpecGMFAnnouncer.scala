package com.deciphernow.server

import java.net.InetAddress
import java.net.URLClassLoader
import java.util.concurrent.Executors

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.google.common.net.InetAddresses
import com.twitter.app.App
import com.twitter.util._
import com.twitter.zk.{ZNode, ZkClient}
import org.apache.zookeeper.KeeperException.NodeExistsException
import org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._
import scala.sys.process.Process
import scala.util.Random

object IntSpecGMFAnnouncerApp extends App {

  GMFNetworkConfigurationResolver.resolveConfiguration

  def main() = {
    val port = sys.env.getOrElse("ANNOUNCE_PORT", "0").toInt
    val scheme = sys.env.getOrElse("ANNOUNCE_SCHEME", "none")
    GMFAnnouncer.announce(port, scheme)
    Thread.currentThread.join
  }

}

case class Announcement(serviceEndpoint: Endpoint, status: String)
case class Endpoint(host: String, port: Int)

@RunWith(classOf[JUnitRunner])
class IntSpecGMFAnnouncer extends FunSpec with Matchers {

  implicit val timer: Timer = new ScheduledThreadPoolTimer(10, Executors.defaultThreadFactory())

  val jar = ClassLoader.getSystemClassLoader.asInstanceOf[URLClassLoader].getURLs.head.toString
  val zookeeper = s"localhost:${sys.props.get("zookeeper.port").getOrElse("2181")}"
  val mapper = (new ObjectMapper() with ScalaObjectMapper)
    .registerModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


  def fetch(path: String): Announcement = {
    val client = ZkClient(zookeeper, Duration.Top, Duration.Top)
    val parent: ZNode = Await.result {
      path.split("/").filter(!_.isEmpty).foldLeft(Future.value(client("/"))) { (parent, child) =>
        parent.flatMap { value =>
          value(child).create(acls = OPEN_ACL_UNSAFE.asScala).rescue {
            case _: NodeExistsException => Future.value(value(child))
          }
        }
      }
    }
    var children = Await.result(parent.getChildren()).children
    while (children.isEmpty) { children = Await.result(parent.getChildren()).children }
    val child = children.head
    val string: String = new String(Await.result(child.getData()).bytes)
    mapper.readValue(string, new TypeReference[Announcement]{})
  }

  def execute(host: String, path: String, scheme: String, port: Int): Process = {
    Process(
      Seq(
        "java",
        "-cp",
        jar,
        s"-Dsun.net.spi.nameservice.provider.1=dns,gmf",
        s"-Dsun.net.spi.nameservice.provider.2=default",
        s"-Dcom.deciphernow.announcement.config.service.forward.hostname=${host}",
        s"-Dcom.deciphernow.server.config.zk.announcementPoint=${path}",
        s"-Dcom.deciphernow.server.config.zk.zookeeperConnection=${zookeeper}",
        "com.deciphernow.server.IntSpecGMFAnnouncerApp"
      ),
      None,
      ("ANNOUNCE_PORT", port.toString),
      ("ANNOUNCE_SCHEME", scheme)
    ).run
  }

  describe("Announcer") {

    describe("announce") {

      describe("when provided a string that is an IP address") {

        describe("and the address resolves via DNS") {

          val host = InetAddresses.toAddrString(InetAddress.getByName("www.google.com"))
          val path = s"/${Random.alphanumeric.take(10).mkString}"
          val scheme = Random.alphanumeric.take(5).mkString
          val port = Random.nextInt(65535)

          it("should announce the address") {
            val process = execute(host, path, scheme, port)
            val data = fetch(s"${path}/${scheme}")
            data.serviceEndpoint.host should equal(host)
            process.destroy()
          }
        }

        describe("and the address does not resolve via DNS") {

          val host = "0.0.0.0"
          val path = s"/${Random.alphanumeric.take(10).mkString}"
          val scheme = Random.alphanumeric.take(5).mkString
          val port = Random.nextInt(65535)

          it("should announce the address") {
            val process = execute(host, path, scheme, port)
            val data = fetch(s"${path}/${scheme}")
            data.serviceEndpoint.host should equal(host)
            process.destroy()
          }
        }
      }

      describe("when provided a string that is not an IP address") {

        val host = "www.google.com"
        val path = s"/${Random.alphanumeric.take(10).mkString}"
        val scheme = Random.alphanumeric.take(5).mkString
        val port = Random.nextInt(65535)

        it("should announce the hostname") {
          val process = execute(host, path, scheme, port)
          val data = fetch(s"${path}/${scheme}")
          data.serviceEndpoint.host should equal(host)
          process.destroy()
        }
      }
    }
  }
}
