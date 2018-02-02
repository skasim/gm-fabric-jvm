package com.deciphernow.server.support

import java.io.File

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

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

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class ValidateSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  val reportNames: Seq[String] = (1 until 19).map(index => s"report-${index}.txt")

  val replacementHostname="Announce HOSTNAME=[]"

  val dropHostnames: Seq[String] = List(1, 2, 3, 4, 5, 6, 17).map(index => s"report-${index}.txt")
  val verifyIpAddressHostname : Seq[String] = List(17).map(index => s"report-${index}.txt")

  "For all test script reports " should " assert accordingly" in {

    val MAX_COUNT = 100

    // Update the 'configure.sh' to return the correct dir if changed here.
    val DEFAULT_DIR = "/tmp"

    // Not a very accurate representation of IPv4 address.
    val ipPattern = "(((2[0-5][0-5])|(1[0-9][0-9])|([1-9][0-9])|([0-9]))\\.|((2[0-5][0-5])|(1[0-9][0-9])|([1-9][0-9])|([0-9])))".r

    // Store each test result that failed. Will be dumped in the end. This way can identify all that failed at once.
    var failed : List[String] = List()

    reportNames.foreach { reportName =>
      val reportFilename = s"${DEFAULT_DIR}/${reportName}"
      val reportFile = new File(reportFilename)
      var reportLines : Array[String] = null
      var validateSeperatly : Option[String] = None
      val builder = new StringBuilder
      reportLines = Source.fromFile(reportFilename).getLines().toArray
      if (dropHostnames.contains(reportName)) {
        validateSeperatly = Option(reportLines(1))
        reportLines(1) = replacementHostname
      }
      val answer = reportLines.mkString("\n")

      // Leave - Used this to acquire answers to new tests.
      // println("ONE-LINE => " + answer)

      validateSeperatly match {
        case Some(v) => {
          val start = v.indexOf("[")
          val end = v.indexOf("]")
          val inspectThis = v.substring(start + 1, end)
          val matches = ipPattern.findAllIn(inspectThis)
          var isAddressGood = false
          var isIpAddress = false
          if (verifyIpAddressHostname.contains(reportName)) {
            isIpAddress = true
            isAddressGood = (matches.length == 4)
          }
          if (!isAddressGood && isIpAddress) {
            failed = (s"For test [${reportName}] an ipAddress should have been returned and it was not. Value = [${inspectThis}]") :: failed
          }
          else if (isAddressGood && !isIpAddress) {
            failed = (s"For test [${reportName}] expected a Hostname and suspect received an ipAddress. Value = [${inspectThis}]") :: failed
          }
        }
        case None => None
      }
      val expectedContent = Source.fromInputStream(getClass.getResourceAsStream(s"/${reportName}")).mkString
      try {
        assertResult(expectedContent)(answer)
      }
      catch {
        case e: Exception => {
          failed = ("Expected [" + expectedContent + "] got [" + answer + "]") :: failed
        }
      }
    }

    // Dump all the tests that failed && exit.
    if (failed.length>0) {
      fail( failed.reverse.foreach( v => println(v)).toString )
    }

    // w00t everything passed.
    assert(true)
  }

}
