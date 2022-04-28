# Thorntail on Kubernetes

The purpose of this example is to demonstrate the following:

- How you can use the `kubernetes-thorntail-starter`.
- How Dekorate detects that this is a web app and automatically configures services.
- How you can end-to-end test the application.
- How you can trigger a Docker build after the compilation.

The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-thorntail-starter</artifactId>
      <version>${project.version}</version>
      <scope>provided</scope>
    </dependency>

Which contains all the required modules, including the annotation processors that detect Thorntail web applications.

The [RestApplication.class](src/main/java/io/dekorate/example/RestApplication.java) is annotated with `@KubernetesApplication` which triggers the resource generation.
This annotation allows the user to trigger a Docker build after the compilation, by passing the system property `dekorate.build=true` to the build:

    mvn clean install -Ddekorate.build=true

Note: Dekorate is not going to generate a Dockerfile for you. It expects to find one in the root of the module.
It also expects to find the `docker` binary pointing to a running Docker daemon.

The Thorntail web application processor will detect our [HelloResource.java](src/main/java/io/dekorate/example/HelloResource.java), and will:

- Add container port 9090.
- Expose port 9090 as a service.

It will also add readiness and liveness probes, as configured in the `@KubernetesApplication` annotation.

Note that default port on which Thorntail web apps are exposed is 8080.
Dekorate detected (in `project-defaults.yml`) that the app is configured to be exposed on 9090, and adjusted the configuration automatically.

## Integration testing

For the purpose of integration testing, the example includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the JUnit5 extension that Dekorate provides, that allows you to run integration tests via the '@KubernetesIntegrationTest' annotation.
The integration test is [ThorntailOnKubernetesIT.java](src/test/java/io/dekorate/example/ThorntailOnKubernetesIT.java) and it demonstrates:

- How you can deploy the application for end to end testing.
- How use can use the Kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install

Note: To run the integration tests, an actual Kubernetes environment is required.
As the integration test requires a Docker build to run, the tests will only run if an existing `Dockerfile` is found, and if the Docker daemon you are logged into is the one Kubernetes uses.
