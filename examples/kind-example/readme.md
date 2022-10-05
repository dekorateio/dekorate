# Kind example

A very simple example that demonstrates the use of `@Kind` in its simplest form.
Check the [Main.java](src/main/java/io/dekorate/example/App.java) which bears the annotation.
To access the `@Kind` annotation you just need to have the following dependency in your
class path:

```
    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kind-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>  
```
```java
@Kind
public class App
{

  public static void main(String[] args) {
     //do stuff
  }
}
```

Users that configure dekorate in the annotationless fashion (via application.properties or application.yaml), can:

```
dekorate.kind.ports[0].name=http
dekorate.kind.ports[0].containerPort=8080
```

or 

```
dekorate:
  kind:
    ports: 
    - name: http
      containerPort: 8080
```


Check, if necessary, the [App.java](src/main/java/io/dekorate/example/App.java).

Compile the project using:

    mvn clean install   


The Kind module will calculate a few values by default:
- ImagePullPolicy will be set to `IfNotPresent`.

You can find the generated deployment under: `target/classes/META-INF/dekorate/kind.yml` that should look like:

```
apiVersion: v1
kind: Service
metadata:
  labels:
    app.kubernetes.io/name: kind-example
    app.kubernetes.io/version: 2.8-SNAPSHOT
  name: kind-example
spec:
  ports:
    - name: http
      port: 80
      targetPort: 8080
  selector:
    app.kubernetes.io/name: kind-example
    app.kubernetes.io/version: 2.8-SNAPSHOT
  type: NodePort
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app.kubernetes.io/version: 2.8-SNAPSHOT
    app.kubernetes.io/name: kind-example
  name: kind-example
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/version: 2.8-SNAPSHOT
      app.kubernetes.io/name: kind-example
  template:
    metadata:
      labels:
        app.kubernetes.io/version: 2.8-SNAPSHOT
        app.kubernetes.io/name: kind-example
    spec: {}
```


