# Overview
This document talks to the step to generate thrift classes.

## Modification of your model.
Every time you make a change to the thrift model you have to generate new source files. These files a Java files.

    $ mvn clean generate-sources
    
In your IDE you may have to sync and or invalidate caches ( IntelliJ ) to have visibility of the newly generated classes.    