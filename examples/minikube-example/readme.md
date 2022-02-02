# Minikube example

A very simple example that demonstrates the use of `@MinikubeApplication` in its simplest form.
Check the [Main.java](src/main/java/io/dekorate/example/App.java) which bears the annotation.
To access the `@MinikubeApplication` annotation you just need to have the following dependency in your
class path:

```
    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>minkube-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>  
```
```java
@MinikubeApplication(ports = @Port(name = "http", containerPort = 8080))
public class App
{

  public static void main(String[] args) {
     //do stuff
  }
}
```

Users that configure dekorate in the annotationless fashion (via application.properties or application.yaml), can:

```
dekorate.minikube.ports[0].name=http
dekorate.minikube.ports[0].containerPort=8080
```

or 

```
dekorate:
  minikube:
    ports: 
    - name: http
      containerPort: 8080
```


Check, if necessary, the [App.java](src/main/java/io/dekorate/example/App.java).

Compile the project using:

    mvn clean install   


The minikube module will calculate a few values by default:
- ImagePullPolicy will be set to `IfNotPresent`.
- Service type will be set to NodePort
- A node port number will be selected from a range between 30000 and 31999.

You can find the generated deployment under: `target/classes/META-INF/dekorate/minikube.yml` that should look like:

```
apiVersion: v1
kind: Service
metadata:
  labels:
    app.kubernetes.io/name: minikube-example
    app.kubernetes.io/version: 2.8-SNAPSHOT
  name: minikube-example
spec:
  ports:
    - name: http
      nodePort: 31992
      port: 80
      targetPort: 8080
  selector:
    app.kubernetes.io/name: minikube-example
    app.kubernetes.io/version: 2.8-SNAPSHOT
  type: NodePort
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app.kubernetes.io/version: 2.8-SNAPSHOT
    app.kubernetes.io/name: minikube-example
  name: minikube-example
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/version: 2.8-SNAPSHOT
      app.kubernetes.io/name: minikube-example
  template:
    metadata:
      labels:
        app.kubernetes.io/version: 2.8-SNAPSHOT
        app.kubernetes.io/name: minikube-example
    spec: {}
```


