# Spring Boot on Kubernetes 

The purpose of this example is to demonstrate the following:

- How you can use the kubernetes-spring-starter.
- How dekorate detects that this is a web app and automatically configures services and probe.
- How you can end-to-end test the application.
- How you can trigger a docker build after the compilation.


The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-spring-starter</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains all the required modules, including the annotation processors that detect spring web applications.

The [Main.class](src/main/java/io/dekorate/example/Main.java) is annotated with `@KubernetesApplication` which triggers the resource generation.
It's also annotated with `@DockerBuild`. This annotation allows the user to trigger a docker build after the compilation, by passing the system property 
`dekorate.build=true` to the build for example:

    mvn clean install -Ddekorate.build=true

Note: Dekorate is not going to generate a Dockerfile for you. It expects to find one in the root of the module and it also expects to find the `docker` binary
pointing to a running docker daemon.


The spring web application processor will detect our [Controller.java](src/main/java/io/dekorate/example/Controller.java), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.

## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@KubernetesIntegrationTest' annotation.
The integration test is [SpringBootOnKubernetesIT.java](src/test/java/io/dekorate/example/SpringBootOnKubernetesIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install
    
Note: To run the integration tests an actual kubernetes environment is required.
As the integration test requires a docker build to run, the tests will only run if an existing `Dockerfile` is found, and if the docker daemon used is the one use by kubernetes.
