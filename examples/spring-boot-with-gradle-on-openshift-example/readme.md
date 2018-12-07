# Spring Boot on Openshift using Gradle (and groovy)

This is a variation of the [Spring Boot with Groovy on Openshift Example](../spring-boot-with-groovy-on-openshift-example) built with gradle.

The purpose of this example is to demonstrate the following:

- How to use ap4k with gradle
- How you can use the openshift-spring-stater.
- How ap4k detects that this is a web app and automatically configures services and probes.
- How you can end-to-end test the application.
- How you can trigger an s2i build after the compilation.


To build the application:

    ./gradlew build
    
The application is using:

     compile("io.ap4k:openshift-spring-starter:${ap4kVersion}")
     annotationProcessor("io.ap4k:openshift-annotations:${ap4kVersion}")

Which contains all the required modules, including the annotation processors that detect spring web applications.


To enable annotation processing in gradle:

    buildscript {
      repositories {
        maven {
          url "https://plugins.gradle.org/m2/"
        }
      }
      dependencies {
        classpath("net.ltgt.gradle:gradle-apt-plugin:0.19")
      }
    }
    
    apply plugin: 'org.junit.platform.gradle.plugin'
    
The [Main.groovy](src/main/groovy/io/ap4k/example/sbonopenshift/Main.groovy) is annotated with `@OpenshiftApplication` which triggers the resource generation.
It's also annotated with `@EnableS2iBuild`. This annotation allows the user to trigger an s2i build after the compilation, by passing the system property 
`ap4k.build=true` to the build for example:

    ./gradlew clean build -Dap4k.build=true

Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing openhisft environment.

The spring web application processor will detect our [Controller.java](src/main/groovy/io/ap4k/example/sbonopenshift/Controller.groovy), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## Integration testing

For the purpose of integration testing it includes:

    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.1.0")
    testImplementation("io.ap4k:openshift-junit:${ap4kVersion}")

This annotation will bring in the junit5 extension that ap4k provides, that allows you to run integration tests via the '@OpenshiftIntegrationTest' annotation.
The integration test is [SpringBootOnOpenshiftIT.java](src/test/groovy/io/ap4k/example/sbonopenshift/SpringBootOnOpenshiftIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

     ./gradlew clean build
    
Note: To run the integration tests an actual openshift environment is required.
