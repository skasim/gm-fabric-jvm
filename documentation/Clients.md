# Overview
This document explains how to leverage the microservice framework client factory as well as how to write RESTful clients.

## Assumptions
These examples assume you have created a test-client module and the correct resources have been assigned.

## Client 
When needing to connect to a microservice with a client that requires performance then leveraging the particular microservices' ThriftClientFactory
to create the client is recommended. This should be done if one microservice needs to communicate with another microservice. The returned client can
be configured for non-SSL or SSL.

Example MyFirstMicroserviceClientFactory ( extends ThriftClientFactory ) : thrift client non-SSL   
    
      def useThriftClientFactory_001() = {
        val client = MyFirstMicroserviceClientFactory.createClient("localhost",30000,"thrift")
        val response : Future[String] = client.ping()
        println("001 :: ping ---> " + Await.result(response))
      }
      
Example MyFirstMicroserviceClientFactory ( extends ThriftClientFactory ) : thrift client 2-Way SSL
      
    def useThriftClientFactory_002() = {
      val client = MyFirstMicroserviceClientFactory.createClient("localhost",
        30000,"thrift",
        TlsConfigUtil.createSslContext(
          new File("test-client/src/main/resources/keystore.jks"),
          "password",
          new File("test-client/src/main/resources/truststore.jks"),
          "password")
        )
      for (i <- 1 to 10) {
        val response: Future[String] = client.ping()
        println("002 :: ping ---> " + Await.result(response))
        Thread.sleep(200)
      }
    }    

The __MyFirstMicroserviceClientFactory__ is generated in your microservice project. The __TlsConfigUtil__ is provided by the __gmf__.


## Create thrift clients using ServiceIface
Writing test clients that do not leverage the microservices client factory.

Example Thrift client non-SSL

    def nonSslExample() : Unit = {
        val client : MyFirstMicroservice.ServiceIface = Thrift.client.newIface(":30000", classOf[MyFirstMicroservice.ServiceIface])
        val response : Future[String] = client.ping()
        System.out.println(response.get)
    }

Example Thrift client 2-Way-SSL

    def sslExample() : Unit = {
        val client = Thrift.client
            .configured(Transport.TLSClientEngine(Some({
                case inet: InetSocketAddress => Ssl.client(createSslContext, inet.getHostName, inet.getPort)
                case _ => Ssl.client(createSslContext)
            }))).newIface[MyFirstMicroservice.ServiceIface](":30000")
        val response = client.ping()
    }
    
    def createSslContext(): SSLContext = {
        TlsConfigUtil.createSslContext(new File("test-client/src/main/resources/keystore.jks"),
          "password",
          new File("test-client/src/main/resources/truststore.jks"),
          "password")
    }

The __TlsConfigUtil__ is provided by __gmf__.

## HTTP

### No AclRestFilter
If no SSL and AclRestFilter is protecting the end-point, then doing a regular curl -XGET to the endpoint will work.

    $ curl -XGET localhost:8888/ping

### ![AclRestFilter protecting endpoint](AclRestFilter.md)

The following RESTful client example:

        package com.somepackage;
        
        import com.deciphernow.server.security.DNHelper;
        import org.apache.http.HttpEntity;
        import org.apache.http.client.methods.CloseableHttpResponse;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.impl.client.CloseableHttpClient;
        import org.apache.http.impl.client.HttpClients;
        
        import java.io.BufferedReader;
        import java.io.InputStream;
        import java.io.InputStreamReader;

        public class RestTestClient {
        
            // Make sure this DN is in the whitelist file.
            static String SSL_CLIENT_S_DN = "CN=some-server, OU=wombat, OU=popper, O=deciphernow, C=US";
            
            // Token has to exist on the authoritative backend.
            static String TOKEN = "cn=hamilton harry, ou=people, ou=widgets, ou=popper, o=acme inc, c=us";
            static String USER_TOKEN = TOKEN;
        
            static DNHelper dnHelper = new DNHelper();
        
            /**
             *
             * @param args
             */
            public static void main(String[] args) {
                RestTestClient client = new RestTestClient();
                try {
                    client.httpHitServer();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        
            /**
             *
             * @param message
             */
            protected void header(String message){
                System.out.println("=======================================================================");
                System.out.println(message);
                System.out.println("=======================================================================");
            }
        
            /**
             *
             * @throws Exception
             */
            protected void httpHitServer() throws Exception {
                header("httpHitServer");
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet("http://localhost:20000/ping");
                httpget.setHeader("user_dn",USER_TOKEN);
                httpget.setHeader("ssl_client_s_dn", SSL_CLIENT_S_DN);
                CloseableHttpResponse response = httpclient.execute(httpget);
                try {
                    HttpEntity entity = response.getEntity();
                    System.out.println("status = " + response.getStatusLine().getStatusCode());
                    int status = response.getStatusLine().getStatusCode();
                    if ((entity != null) && (status == 200 || status == 201)) {
                        InputStream instream = entity.getContent();
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(instream));
                            String responseLine = null;
                            while ((responseLine = in.readLine()) != null) {
                                System.out.println(responseLine);
                            }
                        }
                        finally {
                            instream.close();
                        }
                    }
                    else {
                        // Error
                        System.out.println("\n\n");
                        System.out.println("Status  = " + status);
                        System.out.println("Message = " + response.getStatusLine().getReasonPhrase());
                        System.out.println("\n\n");
                    }
                }
                finally {
                    response.close();
                    httpclient.close();
                }
        
            }
        }

## HTTPS
With this usually just hit the HTTPS endpoints with the browser. Make sure to have the appropriate certificates loaded in the browser. You can implement an SSL client but no example is being provided at this time.
