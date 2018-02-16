package com.deciphernow.server

import java.net.InetAddress
import java.net.URLClassLoader

import com.google.common.net.InetAddresses
import com.twitter.app.App
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner

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

@RunWith(classOf[JUnitRunner])
class IntSpecGMFAnnouncer extends FunSpec with Matchers {

  val jar = ClassLoader.getSystemClassLoader.asInstanceOf[URLClassLoader].getURLs.head.toString
  val zookeeper = s"localhost:${sys.props.get("zookeeper.port").getOrElse("2181")}"

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
      ("ANNOUNCE_SCHEME", scheme),
      ("GMF_IP_ADDRESS", host)
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
            Thread.sleep(10000)
            process.destroy()
          }
        }
      }
    }
  }
}
