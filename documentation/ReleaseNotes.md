
# Release Notes

## gm-fabric-jvm-0.2.1

- Some files in 0.2.0 release did not get versioned correctly.

## gm-fabric-jvm-0.2.0

## Issue #55

- Dockerized services announce container IP instead of host IP. 
   Also required capability to override announcment ports.
   
   See [AnnounceAndBind](AnnounceAndBind.md)
   
## Issue #51

- REST service doesn't announce IP to Zookeeper when using 'enableIpAddressResolution' parameters flag.

## gm-fabric-jvm-0.1.4

### Issue #42

- Fix thrift service discovery in zookeeper by removing postfix identifier to 'thrift' registration.

### Issue #11

- Service installs with __Major.Minor__ API version.
- Assigned default install directory to '/opt/services'.
- Assigned default user 'srvcuser'.
- RPM name generated with the __Major.Minor__ as well as handling 'SNAPSHOT' with 'BuildNumber'.
