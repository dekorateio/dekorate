---
title: Container Resource
description: Container Resources
layout: docs
permalink: /docs/container-resources
---

### Container Resources

Kubernetes allows setting rules about container resources:

- Request CPU: The amount of CPU the container needs.
- Request Memory: The amount of memory the container needs.
- Limit CPU: The maximum amount of CPU the container will get.
- Limit Memory: The maximum amount of memory the container will get.

More information: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers

Dekorate supports these options for both the application container and / or any of the side car containers.

#### Application Container resources

##### Using annotations
There are parameters availbe for `@KubernetesApplication`, `@KnativeApplication` and `@OpenshiftApplication`.

Using the `@KubernetesApplication` one could set the resources like:

```java
import io.dekorate.kubernetes.annotation.ResourceRequirements;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(requestResources=@ResourceRequirements(memory="64Mi", cpu="1m"), limitResources=@ResourceRequirements(memory="256Mi", cpu="5m"))
public class Main {
}
```

In the same spirit it workds for `@KnativeApplication` and `@OpenshiftApplication`.

##### Using properties

Users that prefer to configure dekorate using property configuration can use the following options:

```
dekorate.kubernetes.request-resources.cpu=1m
dekorate.kubernetes.request-resources.memory=64Mi
dekorate.kubernetes.limit-resources.cpu=5m
dekorate.kubernetes.limit-resources.memory=256Mi
```

In a similar manner works for openshift:

```
dekorate.openshift.request-resources.cpu=1m
dekorate.openshift.request-resources.memory=64Mi
dekorate.openshift.limit-resources.cpu=5m
dekorate.openshift.limit-resources.memory=256Mi
```


#### Init Containers

If for any reason the application requires the use of init containers, they can be easily defined using the `initContainer`
property, as demonstrated below.
```java
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(initContainers = @Container(image="foo/bar:latest", command="foo"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

or via `application.properties`:

    dekorate.kubernetes.init-containers[0].image=foo/bar:latest
    dekorate.kubernetes.init-containers[0].command=foo


The [@Container](core/src/main/java/io/dekorate/kubernetes/annotation/Container.java) supports the following fields:

- Image
- Image Pull Policy
- Commands
- Arguments
- Environment Variables
- Mounts
- Probes

#### Sidecars

Similarly, to [init containers](#init-containers) support for sidecars is
also provided using the `sidecars` property. For example:
```java
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(sidecars = @Container(image="jaegertracing/jaeger-agent",
                                             args="--collector.host-port=jaeger-collector.jaeger-infra.svc:14267"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

or via `application.properties`:

    dekorate.kubernetes.sidecars[0].image=jaegertracing/jaeger-agent
    dekorate.kuberentes.args=--collector.host-port=jaeger-collector.jaeger-infra.svc:14267

As in the case of [init containers](#init-containers) the [@Container](core/src/main/java/io/dekorate/kubernetes/annotation/Container.java) supports the following fields:

- Image
- Image Pull Policy
- Commands
- Arguments
- Environment Variables
- Mounts
- Probes

#### Adding the kubernetes annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```
