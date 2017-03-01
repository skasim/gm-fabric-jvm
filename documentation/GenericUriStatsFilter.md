# Overview
This document discusses how to set up the GenericUriStatsFilter for RESTful dynamic urls. 

## Prerequisites

- Should only be used with dynamic URI's are defined in the Controller.

    Dynamic:
    
        get("/:*")
        
    Static:
    
        get("/abc/def")
        

## Step-by-step guide

This filter will capture metrics for URI's that are generically defined in the Controller. This filter should be the last filter in the chain, otherwise the metrics will be skewed.

In the server, when defining the RestfulServer, you would instantiate this filter as follows:

        def rest = Some(new RestServer(
            Seq(new AclRestFilter(accessManager),new GenericUriStatsFilter)),
            Seq(new MyWombatRestController(myWombatManager))
        ))

In the above example it assumes you require Two-Way SSL, thus the AclRestFilter is defined. Make sure to define all filters before this one, __GenericUriStatsFilter__. The next thing to do is identify the URI's that are static. The framework will auto-magically track them. We don't want them to be tracked twice, thus returning incorrect stats on the static URIs. In `parameters.config` set  __`-com.deciphernow.server.config.staticRoutes.ignore=`__ to the explicit URI not to be tracked in this filter.

__Example:__ if `/ping` and `/bonkers` are static routes in that they are defined as `get("/ping")` and `post("/bonkers")` in the Controller, then the property would be configured as follows:

    -com.deciphernow.server.config.staticRoutes.ignore=/ping,/bonkers
    
Make sure to include the leading __`/`__ as well as comma separate the routes.
  
The filter itself has Dynamic logging thus it can be turned on/off via `/admin/Logging/` page. 

