# Overview
This document identifies what has to be upgraded and what issues if any there are.

## Current Twitter Libraries
These are current twitter library versions leveraged in the __gm-fabric__.

    <version.twitter.scrooge>4.14.0</version.twitter.scrooge>
    <version.twitter.finagle>6.42.0</version.twitter.finagle>
    <version.twitter.util>6.41.0</version.twitter.util>
    <version.twitter.server>1.27.0</version.twitter.server>
    <version.twitter.finatra>2.8.0</version.twitter.finatra>

The thrift version is a special version explicitly modified by Twitter. It can be acquired from here: http://maven.twttr.com/


## Microservice Framework code gotcha:

There are several classes, objects that have been explicitly copied out of the Twitter framework. This is because the code is private and we needed access to support the necessary behavior.
When the framework is rev'd to the next greatest version from Twitter, make sure to find the new version of this code. Otherwise capabilities that worked in the old version will not necessarily
work in this new version.

| Class/Object | package location | Twitter location |
|--------------|----------------------|------------------|
| ThriftUtil   | com.deciphernow.server.thrift.ThriftUtil | finagle-thrift/src/main/scala/com/twitter/finagle/rich.scala |
| InputBuffer | com.deciphernow.server.thrift.ThriftMethodStatsFilter | com.twitter.finagle.thrift.InputBuffer |
| GenericUriStatsFilter | com.deciphernow.server.filters.GenericUriStatsFilter | ( Parts of the code copied ) finatra-http/src/main/scala/com/twitter/finatra/http/filters/StatsFilter.scala |

