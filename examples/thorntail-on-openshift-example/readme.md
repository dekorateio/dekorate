# Thorntail on OpenShift

The purpose of this example is to demonstrate the following:

- How you can use the `openshift-thorntail-starter`.
- How Dekorate detects that this is a web app and automatically configures services and routes.
- How you can end-to-end test the application.
- How you can trigger an S2I build after the compilation.

The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-thorntail-starter</artifactId>
      <version>${project.version}</version>
      <scope>provided</scope>
    </dependency>

Which contains all the required modules, including the annotation processors that detect Thorntail web applications.

The [RestApplication.java](src/main/java/io/dekorate/example/thorntailonopenshift/RestApplication.java) is annotated with `@OpenshiftApplication` which triggers the resource generation.
This annotation allows the user to trigger an S2I build after the compilation, by passing the system property `dekorate.build=true` to the build:

    mvn clean install -Ddekorate.build=true

Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing OpenShift environment.

The Thorntail web application processor will detect our [HelloResource.java](src/main/java/io/dekorate/example/thorntailonopenshift/HelloResource.java), and will:

- Add container port 8080.
- Expose port 8080 as a service.

It will also expose the service as a route and add readiness and liveness probes, as configured in the `@OpenshiftApplication` annotation.

## Integration testing

For the purpose of integration testing, the example includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This dependency will bring in the JUnit5 extension that Dekorate provides, that allows you to run integration tests via the `@OpenshiftIntegrationTest` annotation.
The integration test is [ThorntailOnOpenshiftIT.java](src/test/java/io/dekorate/example/thorntailonopenshift/ThorntailOnOpenshiftIT.java) and it demonstrates:

- How you can deploy the application for end to end testing.
- How use can use the Kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install

Note: To run the integration tests, an actual OpenShift environment is required.
