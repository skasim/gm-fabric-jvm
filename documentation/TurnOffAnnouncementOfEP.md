# Overview

In certain situations one might want to not announce an endpoint in zookeeper. To not announce an end-point to zookeeper the attribute has to be explicitly set to `false`. All end-point announcement default to `true`.


## Admin

    -com.deciphernow.server.config.announce.admin=false

## HTTP

    -com.deciphernow.server.config.announce.http=false

## HTTPS

    -com.deciphernow.server.config.announce.https=false
    
## Thrift

    -com.deciphernow.server.config.announce.thrift=false
