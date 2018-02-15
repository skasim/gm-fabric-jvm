# Overview
This document identifies various known issues within the microservice framework.

## Integrating a microservice with S3
This section explains the changes need to be made to integrate Thrift and Amazon's S3 API. Due to transient dependency inclusion a __TApplicationException__ may occur.

### Prerequisites
    
    - Amazon S3 artifact
    
#### Step-by-step guide
When including S3 artifact, it's transient HTTPClient/Core must be overridden otherwise an exception on the Client may occur. In the MODEL pom.xml add the following dependency below. New versions of the AWS S3 SDK may require different configuration.

Add to pom:
    
        <!--
            When adding S3 code base the following HTTP Components MUST be included
            to override.
        -->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.3.5</version>
            <exclusions>
                <exclusion>
                    <groupId>org.apache.httpcomponents</groupId>
                    <artifactId>httpclient</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpcore</artifactId>
            <version>4.3.2</version>
            <exclusions>
                <exclusion>
                    <groupId>org.apache.httpcomponents</groupId>
                    <artifactId>httpcore</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        
#### TApplicationException
If your code worked before including S3 and now with S3 included, you get a message along the lines as below, include the above dependency as specified. In the Example below, the key identifier of the conflict is the __org.apache.thrift.TApplicationException__ and the actual exception __java.lang.NoClassDefFoundError: org/apache/http/conn/scheme/SchemeSocketFactory__

Example stacktrace:
    
        org.apache.thrift.TApplicationException: Internal error processing storeMessageTraffic: 'java.lang.NoClassDefFoundError: org/apache/http/conn/scheme/SchemeSocketFactory'
            at org.apache.thrift.TApplicationException.read(TApplicationException.java:108)
            at com.deciphernow.myproject.thrift.RmtDataStorage$Client.recv_storeMessageTraffic(S3DataStore.java:166)
            ....

         
   
## Microservice administration Units of Measure ( UOM ) issue
To change the UOM for capturing metrics on the thrift endpoints an attribute / value pair must be configured in __parameters.config__. There are multiple units of measure and the default unit of measure is __MILLISECONDS__.
The current available UOM are NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, and DAYS. The issue is that the UOM for metrics is MILLISECONDS but there is no UOM explicitly stating that and if the UOM for Thrift metrics is changed, the end user will not understand that there is a disconnect between UOM on the metrics.json page.

[See Microservice Framework code gotcha](UpgradingFramework.md)
   
