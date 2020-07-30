# Frameworkless Rest API on Kubernetes 


The purpose of this example is to demonstrate the following:

- How you can use the kubernetes-annotations to configure a web app and the services.
- How you can end-to-end test the application.
- How you can trigger an image build after the compilation.


The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains the annotation processors that detect web applications.

The [App.java](src/main/java/io/dekorate/example/App.java) is annotated with `@KubernetesApplication` which triggers the resource generation.

```
@KubernetesApplication(
        name = "frameworkless-k8s",        
        ports = @Port(name = "web", containerPort = 8080),  
        expose = true, 
        host = "fw-app.127.0.0.1.nip.io", 
        imagePullPolicy = ImagePullPolicy.Always 
)
```

- We need to prevent Dekorate that a Kubernetes Service should be created. A Kubernetes Service is a resource providing a single, constant point of entry to our application. It has an IP address and port that never change while the service exists. Dekorate will generate a Kubernetes Service in the manifest if a **`@Port`** is defined.
- **`expose = true`** controls whether the application should be exposed via an Ingress resource accessible from the outside the cluster.
- The host under which the application is going to be exposed. It's used by the Ingress resource to deliver the queries addresed to that host to our service.
- We use **`Always`** in order to be able to use an updated image.

Trigger the manifests generation. Navigate to the directory and run `mvn clean package`. The generated manifests can be found under `target/classes/META-INF/dekorate`.

This annotation allows the user to trigger an image build after the compilation, by passing the system property 
`dekorate.build=true` to the build for example:

    mvn clean install -Ddekorate.build=true
    
Note: Note that a basic Dockerfile is provided in the project base directory, Dekorate is not going to generate a Dockerfile for you . Moreover, It expects to find one in the root of the module and it also expects to find the `docker` binary pointing to a running docker daemon.


## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@KubernetesIntegrationTest' annotation.
The integration test is [RestApiFrameworklessOnK8sIT.java](src/test/java/io/dekorate/example/RestApiFrameworklessOnK8sIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install
    
Note: To run the integration tests an actual kubernetes environment is required.
