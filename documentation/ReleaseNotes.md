
# Release Notes

## gm-fabric-jvm-0.2.2

### Issue #80

- Fixed cloudwatch plugin to acquire memory values though MXBean.

### Issue #79

- Enhanced dependency management.

### Issue #77

- Added documentation for cloudwatch plugin.

### Issue #72

- Updated integration tests to work with CircleCi.

### Issue # 71

- Migration of the cloudwatch plugin into this project.

### Issue #67

- Improved documentation with explicit requirements for local dev environment.

## gm-fabric-jvm-0.2.1

- Some files in 0.2.0 release did not get versioned correctly.

## gm-fabric-jvm-0.2.0

### Issue #55

- Dockerized services announce container IP instead of host IP. 
   Also required capability to override announcment ports.
   
   See [AnnounceAndBind](AnnounceAndBind.md)
   
### Issue #51

- REST service doesn't announce IP to Zookeeper when using 'enableIpAddressResolution' parameters flag.

## gm-fabric-jvm-0.1.4

### Issue #42

- Fix thrift service discovery in zookeeper by removing postfix identifier to 'thrift' registration.

### Issue #11

- Service installs with __Major.Minor__ API version.
- Assigned default install directory to '/opt/services'.
- Assigned default user 'srvcuser'.
- RPM name generated with the __Major.Minor__ as well as handling 'SNAPSHOT' with 'BuildNumber'.
