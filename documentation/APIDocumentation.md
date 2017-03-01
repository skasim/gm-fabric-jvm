# Overview
This documentation talks to how to add aglio to your micro-service project. Realize that the micro-service framework does not come prepackaged with a documentation generator tool. It is up to the developer to add it to their explicit project.

## Base documentation 
The pattern is to define three explicit files, __home.md__, __rest.md__ and __thrift.md__. Add two directories to the __server__ project, __src/main/apiblueprint/resources__ for source documentation files and __src/main/apiblueprint/scripts__ for shell scripts to be called by __maven__. More document files may be added, but at minimum the pattern is to have these three files in your microservice.

| Source file | Description |
| ---------|----|
| home.md | This is REQUIRED since it acts like an index file and points to both rest and thrift pages. |
| rest.md | Describes the RESTful endpoints. |
| thrift.md	| Describes the Thrift endpoints and models. |

#### Tool configuration
The following steps define how to add documentation generation to your microservice project.
- Install aglio.
- Create directories in __server__:
    - src/main/apiblueprint/resources
    - src/main/apiblueprint/scripts
- Add the __convert-blueprint.sh__ script defined below to __src/main/apiblueprint/scripts__.
- Add the __maven-antrun-plugin__ defined below the server __pom.xml__.
- Create files and add them to __src/main/apiblueprint/resources__.
    - home.md
    - rest.md
    - thrift.md
- Update your RestController in the microservice project

## Install aglio on Mac OS X

    ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    brew install npm
    npm install -g aglio

## Shell script

Add __convert-blueprint.sh__ to __src/main/apiblueprint/scripts__
    
    #!/bin/bash -
    
    {
    
    COMMAND=`which aglio`
    if [ "${COMMAND}" == "" ]
    then
      echo "Unable to generate documentation. No conversion tool found."
      exit -1
    fi
    
    SOURCE="${1}"
    DESTINATION="${2}"
    FILE="${3}"
    
    if [ "${SOURCE}" == "" ] || [ "${DESTINATION}" == "" ] || [ "${FILE}" == "" ]
    then
      echo "Cannot generate documention. One or more attributes are empty. See below:"
      echo "SOURCE      = [ ${SOURCE} ]"
      echo "DESTINATION = [ ${DESTINATION} ]"
      echo "FILE        = [ ${FILE} ]"
      exit -1
    fi
    
    
    if [ ! -e "${DESTINATION}" ]
    then
       mkdir -p "${DESTINATION}"
    fi
    
    ${COMMAND} -i "${SOURCE}/${FILE}.md" -o "${DESTINATION}/${FILE}.html"
    
    }
        
## POM changes        
Update the server __pom.xml__ with the following __ant__ tasks.
        
    <plugin>
        <artifactId>maven-antrun-plugin</artifactId>
        <version>1.7</version>
        <executions>
            <execution>
                <phase>process-resources</phase>
                <configuration>
                    <tasks>
                        <exec dir="${project.basedir}" executable="${project.basedir}/src/main/apiblueprint/scripts/convert-blueprint.sh" failonerror="false">
                            <arg line="${project.basedir}/src/main/apiblueprint/resources ${project.basedir}/target/classes/public home"/>
                        </exec>
                        <exec dir="${project.basedir}" executable="${project.basedir}/src/main/apiblueprint/scripts/convert-blueprint.sh" failonerror="false">
                            <arg line="${project.basedir}/src/main/apiblueprint/resources ${project.basedir}/target/classes/public rest"/>
                        </exec>
                        <exec dir="${project.basedir}" executable="${project.basedir}/src/main/apiblueprint/scripts/convert-blueprint.sh" failonerror="false">
                            <arg line="${project.basedir}/src/main/apiblueprint/resources ${project.basedir}/target/classes/public thrift"/>
                        </exec>
                    </tasks>
                </configuration>
                <goals>
                    <goal>run</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
        
If there are images or files to include then add the following __copy__ task to the above plugin configuration and copy all `includes` to `${project.basedir}/target/classes/public/images`.


    <copy todir="${project.basedir}/target/classes/public/images">
        <fileset dir="${project.basedir}/src/main/apiblueprint/resources/images" includes="*.png" />
    </copy> 
               
__Full example:__

    <plugin>
        <artifactId>maven-antrun-plugin</artifactId>
        <version>1.7</version>
        <executions>
            <execution>
                <phase>process-resources</phase>
                <configuration>
                    <tasks>
                        <exec dir="${project.basedir}" executable="${project.basedir}/src/main/apiblueprint/scripts/convert-blueprint.sh" failonerror="true">
                            <arg line="${project.basedir}/src/main/apiblueprint/resources ${project.basedir}/target/classes/public home"/>
                        </exec>

                        <exec dir="${project.basedir}" executable="${project.basedir}/src/main/apiblueprint/scripts/convert-blueprint.sh" failonerror="true">
                            <arg line="${project.basedir}/src/main/apiblueprint/resources ${project.basedir}/target/classes/public rest"/>
                        </exec>
                        <exec dir="${project.basedir}" executable="${project.basedir}/src/main/apiblueprint/scripts/convert-blueprint.sh" failonerror="true">
                            <arg line="${project.basedir}/src/main/apiblueprint/resources ${project.basedir}/target/classes/public thrift"/>
                        </exec>
                        <copy todir="${project.basedir}/target/classes/public/images">
                            <fileset dir="${project.basedir}/src/main/apiblueprint/resources/images" includes="*.png" />
                        </copy>
                    </tasks>
                </configuration>
                <goals>
                    <goal>run</goal>
                </goals>
            </execution>
        </executions>
    </plugin>


## RestController update

If the RESTful routes are explicit then a wildcard route may be used to request all documentation artifacts.

    get("/:*") { request: Request =>
        val realpath = if (request.path.equals("/")) { "/home.html" }
        else {
          request.path
        }
        response.ok.file("/public"+realpath)
    }

If the RESTful routes to the service are wildcarded then there is a conflict between service fullfilment and documentation requests. The following changes have to be made to request the documentation.

- Allow the user not to know the landing page, __home.html__.

        get("/") { request: Request =>
            response.ok.file("/public/home.html")
        }

- Link the physical pages.

        get("/home.html") { request: Request =>
            response.ok.file("/public/home.html")
        }
    
        get("/rest.html") { request: Request =>
            response.ok.file("/public/rest.html")
        }
    
        get("/thrift.html") { request: Request =>
            response.ok.file("/public/thrift.html")
        }

- All images and included files will have to be explicitly routed.

        get("/images/SomeImage.png") { request: Request =>
            response.ok.file("/public/images/SomeImage.png")
        }
        
__Full example:__
        
    get("/") { request: Request =>
        response.ok.file("/public/home.html")
    }
    get("/home.html") { request: Request =>
        response.ok.file("/public/home.html")
    }
    get("/rest.html") { request: Request =>
        response.ok.file("/public/rest.html")
    }
    get("/thrift.html") { request: Request =>
        response.ok.file("/public/thrift.html")
    }
    get("/images/SomeImage.png") { request: Request =>
        response.ok.file("/public/images/SomeImage.png")
    }

