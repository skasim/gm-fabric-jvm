# Overview
The micro service framework contains necessary logic to enable two factor SSL on the REST and Thrift transport. This feature enables application developers to easily support the User/System Access Pattern (PKI Two-Way SSL) patterns without the hassle of trying to inject the necessary dependencies and code needed to support this feature. 

- Prerequisites
    - Must have a running microservice based on the DecipherNow microservice framework. See ![](CreateNewMS.md)
    - Must have a valid test certificate. It must be signed by the CA that was used to sign the truststore.
    
## Step-by-step

### Configure 2-Way SSL

- Acquire the keystore / truststore jks files.
- Copy both *.jks to the `etc` dir of the microservice.
- Edit `parameters.config` in `etc` and add the following:

    Keystore:

        -com.deciphernow.server.config.tls.keyStore=etc/keystore.jks
        -com.deciphernow.server.config.tls.keyStorePass=password
    
    TrustStore:
    
        -com.deciphernow.server.config.tls.trustStore=etc/truststore.jks
        -com.deciphernow.server.config.tls.trustStorePass=password
        
### Changing SSL ports
The RESTful endpoints are served up by two servers, HTTP and HTTPS. Enabling 2-Way SSL only affects HTTPS server.

#### HTTPS

    -com.deciphernow.server.config.rest.httpsPort=:20001

#### Thrift
Once 2-Way SSL is configured then Thrift endpoints are always enabled as 2-Way SSL.

    -com.deciphernow.server.config.thrift.port=:30000
    

    
