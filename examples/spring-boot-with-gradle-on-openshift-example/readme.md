# Spring Boot on OpenShift using Gradle (and groovy)

This is a variation of the [Spring Boot with Groovy on OpenShift Example](../spring-boot-with-groovy-on-openshift-example) built with gradle.

The purpose of this example is to demonstrate the following:

- How to use dekorate with gradle
- How you can use the openshift-spring-stater.
- How dekorate detects that this is a web app and automatically configures services and probes.
- How you can end-to-end test the application.
- How you can trigger an s2i build after the compilation.


To build the application:

    ./gradlew build
    
The application is using:

     compile("io.dekorate:openshift-spring-starter:${dekorateVersion}")
     annotationProcessor("io.dekorate:openshift-annotations:${dekorateVersion}")

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
    
The [Main.groovy](src/main/groovy/io/dekorate/example/sbonopenshift/Main.groovy) is annotated with `@OpenshiftApplication` which triggers the resource generation.
This annotation allows the user to trigger an s2i build after the compilation, by passing the system property 
`dekorate.build=true` to the build for example:

    ./gradlew clean build -Ddekorate.build=true

Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing openhisft environment.

The spring web application processor will detect our [Controller.java](src/main/groovy/io/dekorate/example/sbonopenshift/Controller.groovy), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## Integration testing

For the purpose of integration testing it includes:

    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.1.0")
    testImplementation("io.dekorate:openshift-junit:${dekorateVersion}")

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@OpenshiftIntegrationTest' annotation.
The integration test is [SpringBootOnOpenshiftTests.java](src/test/groovy/io/dekorate/example/sbonopenshift/SpringBootOnOpenshiftTests.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

     ./gradlew clean build
    
Note: To run the integration tests an actual openshift environment is required.
