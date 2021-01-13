# Add hostAliases example 

An example that demonstrates the use of `@KubernetesApplication` in order to add hostAliases property to a deployment.
To access the `@KubernetesApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

So as to add the hostAliases section of the Deployment specification you need pass the `hostAliases` parameter containing the ip address and the list of hostnames (comma separated values) to the `@KubernetesApplication` in the Spring Boot annotated class. The code would look as follow:

```
@KubernetesApplication(hostAliases = {@HostAlias(ip = "127.0.0.1", hostnames = "foo.org,bar.com")})
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```
You can pass multiple `@HostAlias` annotation depending of your needs.

Check, if necessary, the [Main.java](src/main/java/io/dekorate/examples/kubernetes/Main.java).

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/META-INF/dekorate/kubernetes.yml` that should look like:
```---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app.kubernetes.io/name: kubernetes-with-hostaliases-example
    app.kubernetes.io/version: 0.12-SNAPSHOT
  name: kubernetes-example-with-hostaliases
spec:
  replicas: 1
  template:
    metadata:
      labels:
        app.kubernetes.io/name: kubernetes-with-hostaliases-example
        app.kubernetes.io/version: 0.12-SNAPSHOT
    spec:
      hostAliases:
      - hostnames:
        - foo.org
        - bar.com
        ip: 127.0.0.1
      - hostnames:
        - test.com
        ip: 10.0.0.1
```


