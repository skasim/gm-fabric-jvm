package com.deciphernow.server.security

import java.io.{File, PrintWriter}

import org.scalatest.{FlatSpec, Matchers}


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
