---
title: Jaeger
description: Jaeger
layout: docs
permalink: /docs/jaeger
---
### Jaeger annotations

The [jaeger](https://www.jaegertracing.io) annotation processor provides annotations for injecting the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent) into the application pod.

Most of the work is done with the use of the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/dekorate/jaeger/annotation/EnableJaegerAgent.java) annotation.

#### Using the Jaeger Operator

When the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) is available, you set the `operatorEnabled` property to `true`.
The annotation processor will automatically set the required annotations to the generated deployment, so that the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) can inject the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent).

Here's an example:
```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.jaeger.annotation.EnableJaegerAgent;

@KubernetesApplication
@EnableJaegerAgent(operatorEnabled = true)
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```    
##### Manually injection the agent sidecar

For the cases, where the operator is not present, you can use the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/dekorate/jaeger/annotation/EnableJaegerAgent.java) to manually configure the sidecar.

```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.jaeger.annotation.EnableJaegerAgent;

@KubernetesApplication
@EnableJaegerAgent
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```
#### related examples
- [spring boot with jaeger on kubernetes example](examples/spring-boot-with-jeager-on-kubernetes-example)
