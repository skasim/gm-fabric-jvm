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

import com.deciphernow.server.http.response.{HttpForbiddenResponse, HttpUnauthorizedResponse}
import com.twitter.logging.Logger
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request => HttpRequest, Response => HttpResponse}
import com.twitter.util.Future
import com.deciphernow.server.config
import com.deciphernow.server.tls.X509CertificateHelper

/**
 * Created by ghershfield on 157/3/.
 */
class AclRestFilter(manager: ImpersonationAccessManager)
  extends SimpleFilter[HttpRequest,HttpResponse] {

  val log = Logger.get(getClass)
  val dnHelper = new DNHelper

  object X509CertificateTransformer extends X509CertificateHelper

  /**
   *
   * @param request
   * @param service
   * @return
   */
  override def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {

    com.twitter.finagle.transport.Transport.peerCertificate.fold(UserAuthentication.set(None)){cert =>
      val subjectDN = X509CertificateTransformer.translate(cert.getEncoded).getSubjectDN
      UserAuthentication.set(Some(new UserAuthentication(subjectDN.getName)))
    }

    val pki = UserAuthentication.current

    // Do we want to process HTTP requests?
    if (!isHttps(pki) && blockHttp) {
      log.ifInfo("HTTP requests have been configured to be blocked.")
      Future.value(new HttpForbiddenResponse)
    }
    else {
      var user_dn = dnHelper.normalizeDistinguishedNameOption(request.headerMap.get("user_dn"))
      var ssl_client_s_dn = request.headerMap.get("ssl_client_s_dn")
      var external_sys_dn = request.headerMap.get("external_sys_dn")

      if (isHttps(pki)) {
        /*
          DO NOT NORMALIZE the user because it comes from a certificate used directly.
        */
        ssl_client_s_dn = Option(UserAuthentication.current.get.user)
      }

      val transactionType: Array[Transaction.Value] = Array(Transaction.IMPERSONATION)
      val errorMessage = validateHeaders(pki, user_dn, ssl_client_s_dn, external_sys_dn, transactionType)
      val msg =  " transport: " + getTransportType(pki) +
        " user_dn: " + getValue(user_dn) +
        " ssl_client_s_dn: " + getValue(ssl_client_s_dn) + " external_sys_dn: " + getValue(external_sys_dn)
      errorMessage match {
        case Some(_) =>
          log.ifInfo("Transaction: " + transactionType.mkString + " INVALID!" + msg + " " + errorMessage.get)
          UserAuthentication.clear() // Unset value so make sure that nothing gets passed along.
          Future.value(new HttpUnauthorizedResponse(errorMessage.get))
        case _ =>
          if (transactionType(0)==Transaction.IMPERSONATION) {
            UserAuthentication.set(Option(new UserAuthentication(user_dn.get,"")))
          }
          log.ifInfo("Transaction: " + transactionType.mkString + " VALID! UserAuthentication.current: " + UserAuthentication.current.get.user + " " + msg)
          service(request)
      }
    }
  }

  /*

    Define the Transaction enumeration { NORMAL, IMPERSONATION, UNKNOWN }

   */
  object Transaction extends Enumeration {
    type Transaction = Value
    val NORMAL, IMPERSONATION, UNKNOWN = Value
  }

  def getTransportType(v: Option[_]) : String = v.fold("HTTP")(_ => "HTTPS")
  def getValue(v: Option[String]): String =  v.fold("[not-provided] ")(_ =>  "["+dnHelper.normalizeDistinguishedName(v.get.toString).toLowerCase +"]")

  var transactionType = Transaction.UNKNOWN

  /**
   *
   * @param user_dn
   * @param ssl_client_s_dn
   * @param external_sys_dn
   * @return
   */
  def validateHeaders(pki: Option[_], user_dn: Option[_] = None, ssl_client_s_dn: Option[_] = None, external_sys_dn: Option[_] = None, transaction: Array[Transaction.Value]) : Option[String] = {

    if (isHttps(pki)) {
      if (!have(user_dn) && have(ssl_client_s_dn) && !have(external_sys_dn)) {
        // the ssl_client_s_dn is really the User and we don't need to look anything up.
        transaction.update(0,Transaction.NORMAL)
        None
      }
      else if (have(user_dn) && have(ssl_client_s_dn) && have(external_sys_dn)) {
        transaction.update(0,Transaction.IMPERSONATION)
        // Look up ssl_client_s_dn && external_sys_dn in whitelist
        if (!manager.canImpersonateUser(ssl_client_s_dn.get.toString, user_dn.get.toString) ||
          !manager.canImpersonateUser(external_sys_dn.get.toString, user_dn.get.toString)) {
          Some("Unauthorized: Either or both of the ssl_client_s_dn or external_sys_dn are not authorized to impersonate or have access.")
        }
        else { None }
      }
      else if (have(user_dn) && have(ssl_client_s_dn) && !have(external_sys_dn)) {
        transaction.update(0,Transaction.IMPERSONATION)
        // Lookup ssl_client_s_dn
        if (!manager.canImpersonateUser(ssl_client_s_dn.get.toString,user_dn.get.toString)) {
          Some("Unauthorized: The ssl_client_s_dn is not authorized to impersonate or have access.")
        }
        else { None }
      }
      else if (!have(user_dn) && have(ssl_client_s_dn) && have(external_sys_dn)) {
        transaction.update(0,Transaction.IMPERSONATION)
        // Exception we are missing the user_dn that is to be passed along.
        Some("Unathorized: Missing the user_dn.")
      }
      else {
        transaction.update(0,Transaction.UNKNOWN)
        // Exception here it that we don't have user_dn && we don't have external_sys_dn. This is kind of imposible since
        // PKI connection and thus ssl_client_s_dn filled in, there is NO user_dn.
        Some("Unathorized: Invalid connection. Required headers, user_dn and possibly external_sys_dn are missing.")
      }
    }
    else {
      transaction.update(0,Transaction.IMPERSONATION)
      if (have(user_dn) && have(ssl_client_s_dn) && have(external_sys_dn)) {
        if (!manager.canImpersonateUser(ssl_client_s_dn.get.toString, user_dn.get.toString) ||
          !manager.canImpersonateUser(external_sys_dn.get.toString, user_dn.get.toString)) {
          Some("Unauthorized: Either or both of the ssl_client_s_dn or external_sys_dn are not authorized to impersonate or have access.")
        }
        else { None }
      }
      else if (have(user_dn) && have(ssl_client_s_dn)) {
        if (!manager.canImpersonateUser(ssl_client_s_dn.get.toString,user_dn.get.toString)) {
          Some("Unauthorized: The ssl_client_s_dn is not authorized to impersonate or have access.")
        }
        else { None }
      }
      else if (have(user_dn)) {
        Some("Unauthorized: Connection is not authorized to impersonate. Neither ssl_client_s_dn or external_sys_dn where found.")
      }
      else {
        Some("Unauthorized: Invalid connection. Required headers are missing, user_dn, ssl_client_s_dn, and or external_sys_dn.")
      }
    }
  }

  /**
    *
    * @param v
    * @return true if have a value.
    */
  def have(v: Option[_]) : Boolean = v.fold(false)(_ => true)


  lazy val blockHttp: Boolean = config.filter.blockHttp.get.fold(false)(_ => true)

  /**
    *
    * @param v
    * @return
    */
  def isHttps(v: Option[_]) : Boolean = v.fold(false)(_ => true)


}
