
### Junit5 extensions

Dekorate provides two junit5 extensions for:

- Kubernetes
- OpenShift

These extensions are `dekorate` aware and can read generated resources and configuration, in order to manage `end to end` tests
for the annotated applications.

#### Features

- Environment conditions
- Container builds
- Apply generated manifests to test environment
- Inject test with:
  - client
  - application pod

#### Kubernetes extension for Junit5

The kubernetes extension can be used by adding the following dependency:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-junit</artifactId>
  <version>2.6.0</version>
</dependency>
```    
This dependency gives access to [@KubernetesIntegrationTest](testing/kubernetes-junit/src/main/java/io/dekorate/testing/annotation/KubernetesIntegrationTest.java) which is what enables the extension for your tests.

By adding the annotation to your test class the following things will happen:

1. The extension will check if a kubernetes cluster is available (if not tests will be skipped).
2. If `@EnableDockerBuild` is present in the project, a docker build will be triggered.
3. All generated manifests will be applied.
4. Will wait until applied resources are ready.
5. Dependencies will be injected (e.g. KubernetesClient, Pod etc)
6. Test will run
7. Applied resources will be removed.

##### Dependency injection

Supported items for injection:

- KubernetesClient
- Pod (the application pod)
- KubernetesList (the list with all generated resources)

To inject one of this you need a field in the code annotated with [@Inject](testing/core-junit/src/main/java/io/dekorate/testing/annotation/Inject.java).

For example:
```java
@Inject
KubernetesClient client;
```    
When injecting a Pod, it's likely we need to specify the pod name. Since the pod name is not known in advance, we can use the deployment name instead.
If the deployment is named `hello-world` then you can do something like:
```java
@Inject
@Named("hello-world")
Pod pod;
```
Note: It is highly recommended to also add `maven-failsafe-plugin` configuration so that integration tests only run in the `integration-test` phase.
This is important since in the `test` phase the application is not packaged. Here's an example of how it you can configure the project:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>${version.maven-failsafe-plugin}</version>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
      <phase>integration-test</phase>
      <configuration>
        <includes>
          <include>**/*IT.class</include>
        </includes>
      </configuration>
    </execution>
  </executions>
</plugin>
```

#### related examples
- [spring boot on kubernetes example](examples/spring-boot-on-kubernetes-example)

#### OpenShift extension for JUnit5

Similarly, to using the [kubernetes junit extension](#kubernetes-extension-for-junit5) you can use the extension for OpenShift, by adding  [@OpenshiftIntegrationTest](testing/openshift-junit/src/main/java/io/dekorate/testing/annotation/OpenshiftIntegrationTest.java).
To use that you need to add:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-junit</artifactId>
  <version>2.6.0</version>
</dependency>
```    
By adding the annotation to your test class the following things will happen:

1. The extension will check if a kubernetes cluster is available (if not tests will be skipped).
2. A docker build will be triggered.
3. All generated manifests will be applied.
4. Will wait until applied resources are ready.
5. Dependencies will be injected (e.g. KubernetesClient, Pod etc)
6. Test will run
7. Applied resources will be removed.

#### related examples
- [spring boot on openshift example](examples/spring-boot-on-openshift-example)
- [spring boot with groovy on openshift example](examples/spring-boot-with-groovy-openshift-example)
- [spring boot with gradle on openshift example](examples/spring-boot-with-gradle-openshift-example)
