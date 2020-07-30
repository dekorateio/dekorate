# Frameworkless Rest API on OpenShift 


The purpose of this example is to demonstrate the following:

- How you can use the openshift-annotations to configure a web app and the services.
- How you can end-to-end test the application.
- How you can trigger an image build after the compilation.


The application is using:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Which contains the annotation processors that detect web applications.

The [App.java](src/main/java/io/dekorate/example/App.java) is annotated with `@OpenshiftApplication` which triggers the resource generation.

```java
@OpenshiftApplication(
        ports = @Port(name = "web", containerPort = 8080),  
        expose = true, 
)
```
- To tell to Dekorate that a Kubernetes `Service` should be created, then we configure the **`@Port`** parameter to specify the name of the service to be used and port.
- **`expose = true`** controls whether the application should be accessible outside of the cluster and that an Openshift `Route` resource is create.
- The host under which the application is going to be exposed. It's used by the `Route` resource to deliver the queries addresed to that host to our service.
- The parameter **`Always`** of the parameter `ImagePullPolicy` allows deploying or redeploying applications with images updated within the container registry.

Trigger the manifests generation. Navigate to the directory and run `mvn clean package`. The generated manifests can be found under `target/classes/META-INF/dekorate`.

The `@OpenshiftApplication` annotation allows the user to trigger an s2i build after the compilation, by passing the system property 
`dekorate.build=true`

Note: To perform an actual build, the `oc` binary is required to be configured to point to an existing openhisft environment.
So, If you want to trigger an image build after the compilation, you can run the following command:

    mvn clean install -Ddekorate.build=true
    

## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-junit-starter</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@OpenshiftIntegrationTest' annotation.
The integration test is [RestApiFrameworklessOnOpenshiftIT.java](src/test/java/io/dekorate/example/RestApiFrameworklessOnOpenshiftIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install
    
Note: To run the integration tests an actual openshift environment is required.
