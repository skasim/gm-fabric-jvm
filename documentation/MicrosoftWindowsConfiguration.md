# Overview
This document defines how to configure for 32 bit windows.

## Guidance
The commands below will fail on 64 Bit windows by default as the 64-bit Windows version of the Java Service Wrapper are not currently being made available in the Community Edition.  To work around this, modify the my-first-microservice-server.bat file, and comment out the following on lines 54-56 by adding "rem " to the beginning of the line. This will use the 32 bit version of the wrapper instead.

    rem if "%PROCESSOR_ARCHITEW6432%"=="AMD64" goto amd64
    rem if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto amd64
    rem if "%PROCESSOR_ARCHITECTURE%"=="IA64" goto ia64

Save the file and exit.

Next, the service has to be installed before being able to start or check its status. 

Within the command prompt, run:

    ./myfirstmicroservice-server install

This will output the following:

    myfirstmicroservice-server service installed

It is now listed as a windows service with automatic startup using the Local System account named myfirstmicroservice-server.

If you want to uninstall the service in the future, run the following:

    ./myfirstmicroservice-server remove

This will output the following:

    myfirstmicroservice-server service removed.

You may proceed with the remaining guidance for starting the service, checking its status and accessing endpoint.