# Spring Boot with Service Catalog on OpenShift 


The purpose of this example is to demonstrate the following:

- How you can use the openshift-spring-stater.
- How ap4k detects that this is a web app and automatically configures services and probes.
- How you can integrate with the service catalog.
- How you can bind to service instances.
- How you can map the binding to your configuration.
- How you can end-to-end test the application.
- How you can trigger an s2i build after the compilation.


The application is using:

    <dependency>
      <groupId>io.ap4k</groupId>
      <artifactId>openshift-spring-starter</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains all the required modules, including the annotation processors that detect spring web applications.

The [Main.java](src/main/java/io/ap4k/example/sbonopenshift/Main.java) is annotated with `@OpenshiftApplication` which triggers the resource generation.
This annotation allows the user to trigger an s2i build after the compilation, by passing the system property 
`ap4k.build=true` to the build for example:

    mvn clean install -Dap4k.build=true
    
Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing OpenShift environment.

The spring web application processor will detect our [Controller.java](src/main/java/io/ap4k/example/sbonopenshift/Controller.java), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.ap4k</groupId>
      <artifactId>openshift-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that ap4k provides, that allows you to run integration tests via the '@OpenshiftIntegrationTest' annotation.
The integration test is [SpringBootOnOpenshiftIT.java](src/test/java/io/ap4k/example/sbonopenshift/SpringBootOnOpenshiftIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install
    
Note: To run the integration tests an actual OpenShift environment is required.
