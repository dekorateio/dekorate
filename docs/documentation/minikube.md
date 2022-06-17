---
title: Minikube
description: Minikube
layout: docs
permalink: /docs/minikube
---

### Minikube

[@Minikube](https://raw.githubusercontent.com/dekorateio/dekorate/main/annotations/minikube-annotations/src/main/java/io/dekorate/minikube/annotation/Minikube.java) is a more specialized form of [@KubernetesApplication](https://raw.githubusercontent.com/dekorateio/dekorate/main/annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java).
It can be added to your project like:

```java
import io.dekorate.minikube.annotation.Minikube;

@Minikube
public class Main {

    public static void main(String[] args) {
      //Your application code goes here.
    }
}
```

When the project gets compiled, the annotation will trigger the generation of a `Deployment` in both json and yml that
will end up under 'target/classes/META-INF/dekorate'.

#### Adding the minikube annotation processor to the classpath

This module can be added to the project using:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>minikube-annotations</artifactId>
  <version>2.10.0</version>
</dependency>
```

The `@Minikube` annotation comes with a few parameters, which can be used in order to add and customize extra ports. This will trigger the addition of a container port to the `Deployment` but also will trigger the generation of a `Service` resource. By default, Minikube module will generate a service of type `NodePort` and select a port number between 30000 and 31999.

The `@Minikube` annotation can be used in combination with `@KubernetesApplication`. In that case the `@Minikube` configuration will be replaced by the one specified using `@KubernetesApplication`.

**REMINDER**: A complete reference on all the supported properties can be found in the [configuration options guide]({{site.baseurl}}/configuration-guide).

```java
import io.dekorate.minikube.annotation.Minikube;

@Minikube(ports = @Port(name = "http", containerPort = 8080))
public class Main {

    public static void main(String[] args) {
      //Your application code goes here.
    }
}
```
The following `minikube.yml` manifest will be generated:

```yaml
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app.kubernetes.io/name: minikube-example
    app.kubernetes.io/version: 2.9-SNAPSHOT
  name: minikube-example
spec:
  ports:
    - name: http
      nodePort: 31992
      port: 80
      targetPort: 8080
  selector:
    app.kubernetes.io/name: minikube-example
    app.kubernetes.io/version: 2.9-SNAPSHOT
  type: NodePort
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app.kubernetes.io/name: minikube-example
    app.kubernetes.io/version: 2.9-SNAPSHOT
  name: minikube-example
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: minikube-example
      app.kubernetes.io/version: 2.9-SNAPSHOT
  template:
    metadata:
      labels:
        app.kubernetes.io/name: minikube-example
        app.kubernetes.io/version: 2.9-SNAPSHOT
    spec:
      containers:
        - env:
            - name: KUBERNETES_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
          image: default/minikube-example:2.9-SNAPSHOT
          imagePullPolicy: IfNotPresent
          name: minikube-example
          ports:
            - containerPort: 8080
              name: http
              protocol: TCP

```

Check the minikube examples [here](https://github.com/dekorateio/dekorate/tree/main/examples).
