# Overview
This document explains how to configure the pom to generate an RPM.

# Required support software Mac OS X

For the MAVEN RPM plugin to work make sure to execute the following commands:

    $ ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    $ brew install wget
    $ brew install rpm


# Building the RPM
Execute the following command to build the RPM.
        
    $ mvn clean generate-sources package -Prpm
        
The RPM can be found in:

    $ cd server/target/rpm/{name-of-my-service-server}/RPMS/noarch

# Configuration of the project for RPM
The attributes and plugins are located at the root level POM and server POM. If the behaviour provided meets all of your needs then there is minimal configuration. If different behavior is required read the following sections and review the mojohaus rpm plugin documentation to implement your changes.

## Root POM properties
In the root level project pom.xml there are two properties provided. The following properties are located in the root level POM in '\<properties\>' section.

### RPM version number "<rpm.version/>"
The first property will be the version assigned to the RPM. Both this attribute and __<rpm.release/>__ are used to build the final name of the RPM. The attribute __<major.minor/>__ is used appended to the name of the service. This allows for multiple installations of the service on one server. The value assigned should equal the __MAJOR__ __MINOR__ of the defined API.

        <!--
          RPM version cannot have "-SNAPSHOT" && BuildNumber in it.
          Thus one has to manually update the version converting any '-' to '_'.
        -->
        <version.rpm>0.1.0_SNAPSHOT</version.rpm> <!-- This is used in 'server/pom.xml' -->
        <major.minor>0.1</major.minor>
       
## Server POM configuration
There are six properties in '\<properties\>' section of the 'server/pom.xml' that are leveraged through out the maven plugins as well as the shell scripts that get executed during RPM install or removal.

        
        <app.name>${project.artifactId}</app.name>

        <app.runUser>srvcuser</app.runUser>
        <app.groupName>services</app.groupName>

        <rpm.version>${version.rpm}</rpm.version>

        <!--

            This attribute is used in <prefix> and you CANNOT end with a '/'

            todo: Fill in the path that the application is going to install in.
        -->
        <rpm.basePath>/opt/services</rpm.basePath>

### Assign USER "<app.runUser/>"
Of the six provided properties, you only need to fix two of them. For '\<app.runUser/\>' assign it a user by encasing the username in a START and END XML tag.

    Example : User is ctxhistory
    
        <app.runUser>ctxhistory</app.runUser>
       
### Update INSTALL directory "<rpm.install.dir/>"       
Next you have to provide an installation directory so fix: '\<rpm.install.dir\>/opt/.....\</rpm.install.dir\>' to point to a directory. If it does not exist it will create it. Also DO NOT end with a slash!

    Example : Dir is /opt/bing/all-apps-here
    
        <rpm.install.dir>/opt/bing/all-apps-here</rpm.install.dir>
        
## RPM Maven plugin
The following plugin builds the RPM. As it is configured in this framework, there is no required changes. To understand this plugin please read: http://www.mojohaus.org/rpm-maven-plugin/
          
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>rpm-maven-plugin</artifactId>
                <version>2.1-alpha-4</version>
                <configuration>
                    <name>${app.name}-${major.minor}</name>
                    <version>${rpm.version}</version>
                    <release>${buildNumber}</release>
                    <prefix>${rpm.basePath}</prefix>
                    <sourceEncoding>UTF-8</sourceEncoding>
                    <group>Applications/Engineering</group>
                    <defaultDirmode>755</defaultDirmode>
                    <defaultFilemode>644</defaultFilemode>
                    <defaultUsername>${app.runUser}</defaultUsername>
                    <defaultGroupname>${app.groupName}</defaultGroupname>
                    <defineStatements>
                        <defineStatement>_unpackaged_files_terminate_build 0</defineStatement>
                        <defineStatement>_tmppath /tmp</defineStatement>
                        <defineStatement>_binaries_in_noarch_packages_terminate_build 0</defineStatement>
                        <defineStatement>_use_internal_dependency_generator 0</defineStatement>
                        <defineStatement>__os_install_post %{nil}</defineStatement>
                    </defineStatements>
                    <targetOS>linux</targetOS>
                    <needarch>noarch</needarch>
                    <autoProvides>false</autoProvides>
                    <autoRequires>false</autoRequires>
                    <requires>
                        <require>jre &gt;= 1.8</require>
                        <require>initscripts</require>
                    </requires>

                    <preinstallScriptlet>
                        <scriptFile>target/pre_install.sh</scriptFile>
                    </preinstallScriptlet>

                    <postinstallScriptlet>
                        <scriptFile>target/post_install.sh</scriptFile>
                    </postinstallScriptlet>

                    <preremoveScriptlet>
                        <scriptFile>target/pre_remove.sh</scriptFile>
                    </preremoveScriptlet>

                    <postremoveScriptlet>
                        <scriptFile>target/post_remove.sh</scriptFile>
                    </postremoveScriptlet>


                    <mappings>
                        <mapping>
                            <directory>${rpm.basePath}/${app.name}-${major.minor}</directory>
                            <filemode>775</filemode>
                            <sources>
                                <source>
                                    <location>${project.build.directory}/${app.name}-${project.version}-app/${app.name}-${project.version}</location>
                                    <includes>
                                        <include>**/*</include>
                                    </includes>
                                </source>
                            </sources>
                        </mapping>
                        <mapping>
                            <directory>/etc/init.d</directory>
                            <directoryIncluded>false</directoryIncluded>
                            <sources>
                                <softlinkSource>
                                    <location>${rpm.basePath}/${app.name}-${major.minor}/bin/${app.name}</location>
                                    <destination>${app.name}-${major.minor}</destination>
                                </softlinkSource>
                            </sources>
                        </mapping>
                    </mappings>
                </configuration>
            </plugin>        

## Scripts
The shell scripts that the MAVEN RPM plugin leverage uses a resource plugin to set everything up for it.
          
          
          <plugin>
              <artifactId>maven-resources-plugin</artifactId>
              <version>2.6</version>
              <executions>
                  <execution>
                      <id>create-pre-install</id>
                      <phase>validate</phase>
                      <goals>
                          <goal>copy-resources</goal>
                      </goals>
                      <configuration>
                          <outputDirectory>${basedir}/target</outputDirectory>
                          <resources>
                              <resource>
                                  <directory>src/main/package/rpm</directory>
                                  <includes>
                                      <include>pre_install.sh</include>
                                  </includes>
                                  <filtering>true</filtering>
                              </resource>
                          </resources>
                      </configuration>
                  </execution>
                  <execution>
                      <id>create-post-install</id>
                      <phase>validate</phase>
                      <goals>
                          <goal>copy-resources</goal>
                      </goals>
                      <configuration>
                          <outputDirectory>${basedir}/target</outputDirectory>
                          <resources>
                              <resource>
                                  <directory>src/main/package/rpm</directory>
                                  <includes>
                                      <include>post_install.sh</include>
                                  </includes>
                                  <filtering>true</filtering>
                              </resource>
                          </resources>
                      </configuration>
                  </execution>
                  <execution>
                      <id>create-pre-remove</id>
                      <phase>validate</phase>
                      <goals>
                          <goal>copy-resources</goal>
                      </goals>
                      <configuration>
                          <outputDirectory>${basedir}/target</outputDirectory>
                          <resources>
                              <resource>
                                  <directory>src/main/package/rpm</directory>
                                  <includes>
                                      <include>pre_remove.sh</include>
                                  </includes>
                                  <filtering>true</filtering>
                              </resource>
                          </resources>
                      </configuration>
                  </execution>
                  <execution>
                      <id>create-post-remove</id>
                      <phase>validate</phase>
                      <goals>
                          <goal>copy-resources</goal>
                      </goals>
                      <configuration>
                          <outputDirectory>${basedir}/target</outputDirectory>
                          <resources>
                              <resource>
                                  <directory>src/main/package/rpm</directory>
                                  <includes>
                                      <include>post_remove.sh</include>
                                  </includes>
                                  <filtering>true</filtering>
                              </resource>
                          </resources>
                      </configuration>
                  </execution>
              </executions>
          </plugin>
          
This plugin assists in packaging bourne shell scripts that are located in 'src/main/package/rpm'. Each script has been assigned to an explicit script tag for the RPM to execute during it's phases of installation or removal.
The attributes configured in section __Server POM configuration__ are leveraged within all of these scripts.
        
### Pre-Install script
Before the RPM is installed, if the user does NOT exist, create the user. This user is also used for setting ownership.
        
        #!/bin/sh -
        
        
        {
        #
        # Add group.
        HAVE_GROUP=`getent group ${app.groupName}`
        if [ "$HAVE_GROUP" == "" ]
        then
          groupadd ${app.groupName}
        fi
        
        #
        # Create user account.
        HAVE_USER=`getent passwd ${app.runUser}`
        if [ "$HAVE_USER" == "" ]
        then
           useradd --no-create-home --system --group ${app.groupName} --user ${app.runUser}
        fi
        
        #
        # For STIG'd group access enforcement
        GROUP_ACCESS="/etc/security/groupaccess.conf"
        HAVE_STIGGED_GROUPS=`ls $GROUP_ACCESS 2>/dev/null`
        if [ "$HAVE_STIGGED_GROUPS" == "" ]
        then
           echo "${app.groupName}" > $GROUP_ACCESS
           echo "${app.runUser}" >> $GROUP_ACCESS
        else
           ALREADY_HAVE=`grep ${app.groupName} $GROUP_ACCESS 2>/dev/null`
           if [ "$ALREADY_HAVE" == "" ]
           then
             echo "${app.groupName}" >> $GROUP_ACCESS
             echo "${app.runUser}" >> $GROUP_ACCESS
           fi
        fi
        }
        
### Post-Install script
After the RPM is installed, create a service, but don't turn it on.

        #!/bin/sh -
        
        {
        # Create service::
        chkconfig --add ${app.name}-${major.minor}
        }
        
### Pre-Remove script
Before the RPM packaging is removed, if the service is on, stop it. As well, backup the full service. This is done to eventually delete everything but several directories that may container information like configuration or logs that we would want to transfer or review.

        #!/bin/sh -
        
        {
        
        echo "Stopping service [﻿${app.name}-${major.minor} ]..."
        service ${app.name}-${major.minor} stop || true
        
        #
        # Backup the current install. Remove the unneeded directories.
        # Doing this to keep the configuration and logs of the install being removed.
        # May want to re-use the configureation & review the logs.
        #
        APP=${rpm.basePath}/${app.name}-${major.minor}
        if [ -e "${APP}" ]
        then
          cd ${rpm.basePath}
          cp -a ${APP} ${APP}_bkp
          rm -rf ${APP}_bkp/{bin,lib}
        fi
        
        }
        
### Post-Remove script
After the RPM contents have been removed, move the backed-up service to have a timestamp of date time of removal. Then if for some reason the service still persists at the install directory, remove it.

        #!/bin/sh -
        
        #
        # This script takes the backed up install and assignes a Date Timestamp.
        #
        {
           DTS=`date "+_%Y_%m_%d_%H_%M_%S_%s"`
           APP=${rpm.basePath}/${app.name}-${major.minor}
           mv ${APP}_bkp ${APP}_bkp$DTS
        }