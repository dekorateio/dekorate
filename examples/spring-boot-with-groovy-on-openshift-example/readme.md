# Spring Boot on OpenShift using Groovy

This is a variation of the [Spring Boot on OpenShift Example](../spring-boot-on-openshift-example) written in groovy.


The purpose of this example is to demonstrate the following:

- How you can use the openshift-spring-stater.
- How dekorate detects that this is a web app and automatically configures services and probes.
- How you can end-to-end test the application.
- How you can trigger an s2i build after the compilation.


The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-spring-starter</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains all the required modules, including the annotation processors that detect spring web applications.

The [Main.groovy](src/main/groovy/io/dekorate/example/Main.groovy) is annotated with `@OpenshiftApplication` which triggers the resource generation.
This annotation allows the user to trigger an s2i build after the compilation, by passing the system property 
`dekorate.build=true` to the build for example:

    mvn clean install -Ddekorate.build=true

Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing openhisft environment.

The spring web application processor will detect our [Controller.java](src/main/groovy/io/dekorate/example/Controller.groovy), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@OpenshiftIntegrationTest' annotation.
The integration test is [SpringBootOnOpenshiftIT.java](src/test/groovy/io/dekorate/example/SpringBootOnOpenshiftIT.groovy) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install
    
Note: To run the integration tests an actual openshift environment is required.
