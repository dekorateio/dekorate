---
title: Prometheus
description: Prometheus
layout: docs
permalink: /docs/prometheus
---
### Prometheus annotations

The [prometheus](https://prometheus.io/) annotation processor provides annotations for generating prometheus related resources.
In particular, it can generate [ServiceMonitor](annotations/prometheus-annotations/src/main/java/io/dekorate/prometheus/model/ServiceMonitor.java) which are used by the
[Prometheus Operator](https://github.com/coreos/prometheus-operator) in order to configure [prometheus](https://prometheus.io/) to collect metrics from the target application.

This is done with the use of [@EnableServiceMonitor](annotations/prometheus-annotations/src/main/java/io/dekorate/prometheus/annotation/EnableServiceMonitor.java) annotation.

Here's an example:
```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.prometheus.annotation.EnableServiceMonitor;

@KubernetesApplication
@EnableServiceMonitor(port = "http", path="/prometheus", interval=20)
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```
The annotation processor, will automatically configure the required selector and generate the ServiceMonitor.
Note: Some framework integration modules may further decorate the ServiceMonitor with framework specific configuration.
For example, the Spring Boot module will decorate the monitor with the Spring Boot specific path, which is `/actuator/prometheus`.

#### related examples
- [spring boot with prometheus on kubernetes example](https://github.com/dekorateio/dekorate/tree/main/examples/spring-boot-with-prometheus-on-kubernetes-example)
