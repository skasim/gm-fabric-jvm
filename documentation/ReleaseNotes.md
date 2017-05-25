
# Release Notes

## gm-fabric-jvm-0.1.4

### Issue #42

- Fix thrift service discovery in zookeeper by removing postfix identifier to 'thrift' registration.

### Issue #11

- Service installs with __Major.Minor__ API version.
- Assigned default install directory to '/opt/services'.
- Assigned default user 'srvcuser'.
- RPM name generated with the __Major.Minor__ as well as handling 'SNAPSHOT' with 'BuildNumber'.
