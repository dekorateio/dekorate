# How to use a custom image name examples

An example that demonstrates the use of `@DockerBuild` in order to add a custom image name.
To access the `@DockerBuild` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>docker-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Or a project already containing it like:


    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
 
    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>knative-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

By default the docker image name used is a combination of:

- group
- name 
- version

as extraced by the environment or explicitly configured by the user. So the image name is something like: `${group}/${name}:${version}`.
If you need to use a custom name without having to edit any of the properties above, you can use the `image` property of the `@DockerBuild` annotation.

```
@KubernetesApplication
@DockerBuild(image="foo/bar:baz")
public class Main {

  public static void main(String[] args) {
     //do stuff
  }
}
```

Users that configure dekorate in the annotationless fashion (via application.properties or application.yaml), can:

```
dekorate.docker.image=foo/bar:baz
```

or 

```
dekorate:
  docker:
    image: foo/bar:baz
```


Check, if necessary, the [Main.java](src/main/java/io/dekorate/examples/kubernetes/Main.java).

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/META-INF/dekorate/kubernetes.yml` that should look like:

```---
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app.kubernetes.io/name: kubernetes-example-with-custom-image-name
    app.kubernetes.io/version: 0.13-SNAPSHOT
  name: kubernetes-example-with-custom-image-name
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: kubernetes-example-with-custom-image-name
      app.kubernetes.io/version: 0.13-SNAPSHOT
  template:
    metadata:
      labels:
        app.kubernetes.io/name: kubernetes-example-with-custom-image-name
        app.kubernetes.io/version: 0.13-SNAPSHOT
    spec:
      containers:
        image: foo/bar:baz
        imagePullPolicy: IfNotPresent
        name: kubernetes-example-with-custom-image-name

```


