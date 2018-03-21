# Overview
We make no assumptions as to what VCS product you use. Therefore we do not generate any explicit ignore files. As a developer you should know the name of the ignore file per the VCS product you are using.

## Ignore 
The framework uses the `buildnumber-maven-plugin`. On the very first build of your microservice, it will generate a file `buildNumber.properties` in the `server` module. Every time the project is built, the value will be incremented. You most likely do not want to track this. Especially if there are multiple developers working on the same service. Thus there will be constant `merge conflicts`.

