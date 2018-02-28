
# Release Notes


## gm-fabric-jvm-0.2.5

### Issue 135

- Unable to upload files greater than 5.2MB.

## gm-fabric-jvm-0.2.4

### Issue 112

- Using 'enableIpAddressResolution' was not registering IP address if DNS resolution is available.

## gm-fabric-jvm-0.2.3

* Key is that attributes can now be referenced across all modules. No need to include a separate set of properties to be loaded by a Java properties loader.
* The ability to assign environment variable to all String based attributes.
* Improved documentation.
* RestController tests get generated in a microservice now.

### Issue 101

- Framework adds references to `core` && `config` to all modules of a generated service.

### Issue #99

- Parameters that are __String__ defined can be set by a system environment variable.

### Issue #93/94

- Document all changes to framework.

### Issue #92

- Update parameters.config to have all available parameters and documentation.

### Issue #91

- Add Unit Test to RestController in a generated microservice.

### Issue #89

- Fix plugin dependency management.

### Issue #85

- Remove post RPM install script.

### Issue #84

- Move Config out of core into it's own module called 'config'

-------
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

-------

## gm-fabric-jvm-0.2.1

- Some files in 0.2.0 release did not get versioned correctly.

-------

## gm-fabric-jvm-0.2.0

### Issue #55

- Dockerized services announce container IP instead of host IP. 
   Also required capability to override announcment ports.
   
   See [AnnounceAndBind](AnnounceAndBind.md)
   
### Issue #51

- REST service doesn't announce IP to Zookeeper when using 'enableIpAddressResolution' parameters flag.

-------

## gm-fabric-jvm-0.1.4

### Issue #42

- Fix thrift service discovery in zookeeper by removing postfix identifier to 'thrift' registration.

### Issue #11

- Service installs with __Major.Minor__ API version.
- Assigned default install directory to '/opt/services'.
- Assigned default user 'srvcuser'.
- RPM name generated with the __Major.Minor__ as well as handling 'SNAPSHOT' with 'BuildNumber'.
