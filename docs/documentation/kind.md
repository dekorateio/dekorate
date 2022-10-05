---
title: Kind
description: Kind
layout: docs
permalink: /docs/kind
---

### Kind

[@Kind](https://raw.githubusercontent.com/dekorateio/dekorate/main/annotations/kind-annotations/src/main/java/io/dekorate/kind/annotation/Kind.java) is a more specialized form of [@KubernetesApplication](https://raw.githubusercontent.com/dekorateio/dekorate/main/annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java).
It can be added to your project like:

```java
import io.dekorate.kind.annotation.Kind;

@Kind
public class Main {

    public static void main(String[] args) {
      //Your application code goes here.
    }
}
```

When the project gets compiled, the annotation will trigger the generation of a `Deployment` in both `kind.json` and `kind.yml` that
will end up under 'target/classes/META-INF/dekorate'. Also, if the deployment is enabled, it will automate the process of loading images to the cluster
when performing container image builds.

#### Adding the kind annotation processor to the classpath

This module can be added to the project using:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kind-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

The `@Kind` annotation can be used in combination with `@KubernetesApplication`. In that case the `@Kind` configuration will take precedence over the one specified using `@KubernetesApplication` in the generated Kind manifests.

**REMINDER**: A complete reference on all the supported properties can be found in the [configuration options guide]({{site.baseurl}}/configuration-guide).

Check the Kind example [here](https://github.com/dekorateio/dekorate/tree/main/examples/kind-example).
