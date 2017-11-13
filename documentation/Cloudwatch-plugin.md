# Overview

This plugin enables the collection and publishing additional cloudwatch metrics for GM-Fabric-JVM services to AWS.

## Add to your project
Add the following dependency to the __sever__ pom.

    <dependency>
        <groupId>com.deciphernow</groupId>
        <artifactId>gm-fabric-cloudwatch</artifactId>
        <version>${version.gm-fabric}</version>
    </dependency>
    
## Configurable Properties
---------------------
These go in etc/parameters.config of the service.
These properties can also be provided as a cli parameter.

| Property Name | Default value | Optional? | Description |
| --- | --- | --- | --- |
| com.deciphernow.fabric.cloudwatch.period | 300 | yes | Interval, in seconds, between publishing metrics |
| com.deciphernow.fabric.cloudwatch.region | us-east-1 | yes | AWS region |
| com.deciphernow.fabric.cloudwatch.namespace | microservice | yes | namespace to publish to cloudwatch |
| com.deciphernow.fabric.cloudwatch.metricNames | Set() | no | Set of metrics names, comma separated. See [list] |
| com.deciphernow.fabric.cloudwatch.pidName | wrapper.java.pid | yes | The name of the running java process |
| com.deciphernow.fabric.cloudwatch.serviceName | not-configured | yes | The name of the captured service metrics |

### Example parameters.config
---------------------
```
# Just for testing. Use the default, 300 seconds, for production.
-com.deciphernow.fabric.cloudwatch.period=5
# Just for testing. Use the default, us-east-1, for development. Use the appropriate region for C2S.
-com.deciphernow.fabric.cloudwatch.region=us-west-1
# Several are listed just for testing. Pick 1 (or maybe 2) for production.
-com.deciphernow.fabric.cloudwatch.metricNames=process/cpu/percent,process/memory/mb,process/memory/percent,srv/load,srv/request_latency_ms.p90
-com.deciphernow.fabric.cloudwatch.serviceName=birds-api
-com.deciphernow.fabric.cloudwatch.pidName=birds
```

#### **Note:**
The size of a PutMetricData request is limited to 8 KB for HTTP GET requests and 40 KB for HTTP POST requests.
Since CloudWatch is not a general purpose reporting solution, is billed per metric, and has a low retention period, the
assumption is that a limited number of metrics will be pushed to CloudWatch. Therefore, this library makes no effort to
split/batch PutMetricData requests to stay under CloudWatch's limits - takeaway: be cognizant of the # of metrics sent
to CloudWatch.


HTTP Admin Endpoint
-------------------
The CloudWatch publisher returns info/stats at: /admin/metrics/cloudwatch.json


AWS CloudWatch Notes
--------------------
Aggregation can only occur on custom metrics if they have the same namespace, name, and dimensions. See:
- http://docs.aws.amazon.com/cli/latest/reference/cloudwatch/get-metric-statistics.html
- http://docs.aws.amazon.com/AmazonCloudWatch/latest/DeveloperGuide/GetSingleMetricAllDimensions.html