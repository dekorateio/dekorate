# Spring Boot on OpenShift with Jib 

The purpose of this example is to demonstrate the following:

- How you can use the openshift-spring-stater.
- How dekorate detects that this is a web app and automatically configures services and probes.
- How you can end-to-end test the application.
- How you can trigger a docker build after the compilation.
- How to configure DeploymentConfig to use images from remote registries.


The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-spring-starter</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains all the required modules, including the annotation processors that detect spring web applications.
It also uses:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>jib-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which enables docker builds.

The [Main.java](src/main/java/io/dekorate/example/sbonopenshift/Main.java) is annotated with `@SpringBootApplication` which triggers the resource generation.
This annotation allows the user to trigger an s2i build after the compilation, by passing the system property 
`dekorate.build=true` to the build for example:

    mvn clean install -Ddekorate.build=true
    
Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing openhisft environment.

The spring web application processor will detect our [Controller.java](src/main/java/io/dekorate/example/sbonopenshift/Controller.java), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## Disable the s2i build

To disable the s2i build, either add the annotation `@S2iBuild(enabled=false)` or the the property `dekorate.s2i.enabled=false` in `application.properties`.
Users that make use of `application.yml` may use:

```yml
dekorate:
  s2i:
    enabled: false
```

## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@OpenshiftIntegrationTest' annotation.
The integration test is [SpringBootOnOpenshiftIT.java](src/test/java/io/dekorate/example/sbonopenshift/SpringBootOnOpenshiftIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install
    
Note: To run the integration tests an actual openshift environment is required.
