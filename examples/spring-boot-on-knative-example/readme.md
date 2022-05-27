# Spring Boot on Knative 

The purpose of this example is to demonstrate the following:

- How you can use the knative-spring-stater.
- How dekorate detects that this is a web app and automatically configures services and probe.
- How you can end-to-end test the application.
- How you can trigger a docker build after the compilation.


The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>knative-spring-starter</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains all the required modules, including the annotation processors that detect spring web applications.

The [Main.class](src/main/java/io/dekorate/example/Main.java) is annotated with `@KnativeApplication` which triggers the resource generation.
Out of the box, the `kantive-spring-starter` comes with `docker` support. This allows the user to trigger a docker build after the compilation, by passing the system property 
`dekorate.build=true` to the build for example:

    mvn clean install -Ddekorate.build=true

Note: Dekorate is not going to generate a Dockerfile for you. It expects to find one in the root of the module and it also expects to find the `docker` binary
pointing to a running docker daemon.


The spring web application processor will detect our [Controller.java](src/main/java/io/dekorate/example/Controller.java), and will:

- add container port 8080
- expose port 8080 as a service
- add readiness and liveness probes.
