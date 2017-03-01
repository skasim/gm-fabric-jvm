#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}

import java.io.File

import ${package}.rest.${appName}RestController
import ${package}.thrift.${appName}ThriftService

import com.deciphernow.server.{GMFabricServer, RestServer, ThriftServer}
import com.deciphernow.server.Implicits._

import scala.concurrent.duration.Duration

object ${appName} extends GMFabricServer {

  val ${appName.substring(0,1).toLowerCase()}${appName.substring(1)}Manager = new ${appName}Manager

  // When using impersonating security filters, we need an access manager
  //var accessManager: FileWhitelistImpersonationAccessManager = _

  // The access manager will require a whitelist file.  This is one way to use configuration for the file path
  //val whitelistFile = flag[File]("acl.whitelist.file", "ACL whitelist file for user impersonation")

  // If we want to create the access manager, do it in the premain block like this.
  // Note we need to do it outside the class body (in premain) because flag parsing occurrs later
  premain {
    //accessManager = new FileWhitelistImpersonationAccessManager(
    //   whitelistFile(), Duration(1, "minute")
    //)
  }

  /*
    Assign None to server if no server is going to be defined.
    def thrift = None
   */
  def thrift = Some(new ThriftServer(
    Nil,
    new ${appName}ThriftService(${appName.substring(0,1).toLowerCase()}${appName.substring(1)}Manager)
  ))

  /*
    A rest server is always required since the admin server is
    instantiated here.
 */
  def rest = Some(new RestServer(
    Nil,
    Seq(new ${appName}RestController(${appName.substring(0,1).toLowerCase()}${appName.substring(1)}Manager))
  ))

}

