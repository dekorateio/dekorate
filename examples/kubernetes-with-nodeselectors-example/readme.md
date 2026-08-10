# Add nodeSelector example 

An example that demonstrates the use of `@KubernetesApplication` in order to add nodeSelector property to a deployment.
To access the `@KubernetesApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

So as to add the nodeSelector section of the Deployment specification you need pass the `nodeSelector` parameter containing they key and the value of the `nodeSelector` `@KubernetesApplication` in the Spring Boot annotated class. The code would look as follow:

```
@KubernetesApplication(nodeSelector = @NodeSelector(key = "diskType", value = "ssd"))
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```
Check, if necessary, the [Main.java](src/main/java/io/dekorate/example/Main.java).

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/META-INF/dekorate/kubernetes.yml` that should look like:
```---
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    app.dekorate.io/vcs-url: <<unknown>>
    app.dekorate.io/commit-id: 31df9b1860543a31fc710f8461b160ea65ce7c7a
  labels:
    app.kubernetes.io/name: kubernetes-with-nodeselectors-example
    app.kubernetes.io/version: 999-SNAPSHOT
  name: kubernetes-with-nodeselectors-example
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: kubernetes-with-nodeselectors-example
      app.kubernetes.io/version: 999-SNAPSHOT
  template:
    metadata:
      annotations:
        app.dekorate.io/vcs-url: <<unknown>>
        app.dekorate.io/commit-id: 31df9b1860543a31fc710f8461b160ea65ce7c7a
      labels:
        app.kubernetes.io/name: kubernetes-with-nodeselectors-example
        app.kubernetes.io/version: 999-SNAPSHOT
    spec:
      containers:
        - env:
            - name: KUBERNETES_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
          image: vinche/kubernetes-with-nodeselectors-example:999-SNAPSHOT
          imagePullPolicy: IfNotPresent
          name: kubernetes-with-nodeselectors-example
      nodeSelector:
        diskType: ssd
```


