package com.deciphernow.server.security

import java.security.cert.X509Certificate

object ImpersonationUtil {
  private val dnHelper = new DNHelper

  class Caller(
                // distinguishedName is the unique identity of a user
                val distinguishedName: String,
                // userDistinguishedName holds the value passed in header USER_DN
                val userDistinguishedName: String,
                // externalSystemDistinguishedName holds the value passed in header EXTERNAL_SYS_DN
                val externalSystemDistinguishedName: String,
                // commonName is the CN value part of the DistinguishedName
                //val commonName: String
              )

  /**
   * getCaller creates a description of the (possibly impersonated) identity of the initiator of an incoming request.
   *
   * An important assumption is that there exists a trustworthy, TLS-terminating proxy between the replying service
   * and the application making the request.
   *
   * The proxy is expected to provide two headers:
   *
   * USER_DN
   *   The effective (possibly impersonated) Distinguished Name of requesting application
   * EXTERNAL_SYS_DN
   *   The Distinguished Name taken from the client certificate
   * 
   * An x509 certificate can be provided to use as a fallback when a USER_DN header is not present, in which case the DN
   * from the cert will be used. This should only be necessary in the unlikely scenario where you need to allow an
   * application to bypass the trusted proxy and establish a direct TLS connection to your service.
  */
  def getCaller(userDN: String, externalSysDN: String, cert: X509Certificate): Caller = {
    var distinguishedName = userDN
    if (userDN != "") {
      distinguishedName = userDN
    } else if (cert != null) {
      distinguishedName = cert.getSubjectX500Principal().getName()
    }
    distinguishedName = dnHelper.normalizeDistinguishedName(distinguishedName)

    return new Caller(
      distinguishedName,
      userDN,
      externalSysDN
    )
  }
}
