package com.deciphernow.server

import java.net.URLClassLoader

import com.twitter.app.App
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, Matchers}

import scala.util.Random
import sys.process._

package config {
  import com.twitter.app.GlobalFlag
  object method extends GlobalFlag[String]("method", "The method to invoke.")
}

object Test extends App {

  GMFNetworkConfigurationResolver.resolveConfiguration

  def main() = {
    val method = com.deciphernow.server.config.method.apply()
    val value = GMFNetworkConfigurationResolver.getClass.getMethod(method).invoke(GMFNetworkConfigurationResolver)
    Console.out.print(value)
  }

}

trait Setting { val key: String; val value: String }
case class Property(key: String, value: String) extends Setting
case class Variable(key: String, value: String, expected: String) extends Setting
case class Method(name: String, default: String, settings: Seq[Setting])

@RunWith(classOf[JUnitRunner])
class IntSpecGMNetworkConfigurationResolver extends FunSpec with Matchers {

  val jar = ClassLoader.getSystemClassLoader.asInstanceOf[URLClassLoader].getURLs.head.toString

  def randomPort = {
    Random.nextInt(65535).toString
  }

  def randomVariable = {
    Random.alphanumeric.take(10).mkString.toUpperCase
  }

  val methods = Seq(
    Method(
      "getAnnounceAdminPort",
      "9990",
      Seq(
        Property("com.deciphernow.server.config.admin.port", randomPort),
        Variable("com.deciphernow.server.config.os.env.adminPort", randomVariable, randomPort),
        Property("com.deciphernow.announcement.config.service.forward.adminPort", randomPort),
        Variable("com.deciphernow.announcement.config.os.env.adminPort", randomVariable, randomPort)
      )
    ),
    Method(
      "getAnnounceHttpPort",
      "8888",
      Seq(
        Property("com.deciphernow.server.config.rest.httpPort", randomPort),
        Variable("com.deciphernow.server.config.os.env.httpPort", randomVariable, randomPort),
        Property("com.deciphernow.announcement.config.service.forward.httpPort", randomPort),
        Variable("com.deciphernow.announcement.config.os.env.httpPort", randomVariable, randomPort)
      )
    ),
    Method(
      "getAnnounceHttpsPort",
      "8999",
      Seq(
        Property("com.deciphernow.server.config.rest.httpsPort", randomPort),
        Variable("com.deciphernow.server.config.os.env.httpsPort", randomVariable, randomPort),
        Property("com.deciphernow.announcement.config.service.forward.httpsPort", randomPort),
        Variable("com.deciphernow.announcement.config.os.env.httpsPort", randomVariable, randomPort)
      )
    ),
    Method(
      "getAnnounceThriftPort",
      "9090",
      Seq(
        Property("com.deciphernow.server.config.thrift.port", randomPort),
        Variable("com.deciphernow.server.config.os.env.thriftPort", randomVariable, randomPort),
        Property("com.deciphernow.announcement.config.service.forward.thriftPort", randomPort),
        Variable("com.deciphernow.announcement.config.os.env.thriftPort", randomVariable, randomPort)
      )
    ),
    Method(
      "getBindAdminPort",
      ":9990",
      Seq(
        Property("com.deciphernow.server.config.admin.port", s":${randomPort}"),
        Variable("com.deciphernow.server.config.os.env.adminPort", randomVariable, s":${randomPort}")
      )
    ),
    Method(
      "getBindHttpPort",
      ":8888",
      Seq(
        Property("com.deciphernow.server.config.rest.httpPort", s":${randomPort}"),
        Variable("com.deciphernow.server.config.os.env.httpPort", randomVariable, s":${randomPort}")
      )
    ),
    Method(
      "getBindHttpsPort",
      ":8999",
      Seq(
        Property("com.deciphernow.server.config.rest.httpsPort", s":${randomPort}"),
        Variable("com.deciphernow.server.config.os.env.httpsPort", randomVariable, s":${randomPort}")
      )
    ),
    Method(
      "getBindThriftPort",
      ":9090",
      Seq(
        Property("com.deciphernow.server.config.thrift.port", s":${randomPort}"),
        Variable("com.deciphernow.server.config.os.env.thriftPort", randomVariable, s":${randomPort}")
      )
    )
  )

  def execute(method: String, variables: Seq[Variable], properties: Seq[Setting]): String = {
    Process(
      Seq(
        Seq("java", "-cp", jar, s"-Dcom.deciphernow.server.config.method=${method}"),
        properties.map(property => s"-D${property.key}=${property.value}"),
        Seq("com.deciphernow.server.Test")
      ).flatten,
      None,
      variables.map(variable => (variable.value, variable.expected)):_*
    ).!!.trim
  }

  methods.foreach { method =>

    describe(s"GMFNetworkConfigurationResolver.${method.name}") {

      describe("when no environment variables or system properties are set") {

        it("should return the default") {
          execute(method.name, Seq(), Seq()) should equal(method.default)
        }
      }

      method.settings.foldLeft[Seq[Setting]](Seq.empty[Setting]) { (settings, setting) =>

        val current = settings :+ setting
        val variables = current.filter(_.isInstanceOf[Variable]).map(_.asInstanceOf[Variable])
        val expected = if (setting.isInstanceOf[Variable]) setting.asInstanceOf[Variable].expected else setting.value

        describe(s"when the following are set ${current}") {

          it(s"should return ${expected}") {
            execute(method.name, variables, current) should equal(expected)
          }
        }
        current
      }
    }
  }
}

