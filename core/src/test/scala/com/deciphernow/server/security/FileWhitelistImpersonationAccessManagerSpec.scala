package com.deciphernow.server.security


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

import java.io.{File, PrintWriter}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class FileWhitelistImpersonationAccessManagerSpec extends FlatSpec with Matchers {

  val tempFile: File = createTempAuthFile()

  val manager: FileWhitelistImpersonationAccessManager = new FileWhitelistImpersonationAccessManager(tempFile)
  val whitelist: Set[String] = manager.loadWhitelist()


  "A FileWhitelistImpersonationAccessManager" should "allow users in the file" in {
    whitelist should contain("foo")
    whitelist should contain("bar")
  }

  it should "not allow users not in the file" in {
    whitelist should not contain("baz")
  }


  private def createTempAuthFile(): File = {
    val tempFile: File = File.createTempFile("gmf-example", getClass.getName)
    tempFile.deleteOnExit()
    val pw = new PrintWriter(tempFile)
    pw.println("foo")
    pw.println("bar")
    pw.close

    tempFile
  }

}
