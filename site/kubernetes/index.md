---
layout: base
---
## Kubernetes

## Setting up

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>2.2.1</version>
</dependency>
```

Then add the `@Dekorate` annotation to one of your Java source files. 

```java
package org.acme;

import io.dekorate.annotation.Dekorate;

@Dekorate
public class Application {
}
```

Note: It doesn't have to be the `Main` class.
Next time you perform a build, using something like:

## Building

    mvn clean package
    
The generated manifests can be found under `target/classes/META-INF/dekorate`.


## Screencast

![asciicast](/assets/images/dekorate-vertx-hello-world.gif "Dekorate Vert.X Hello World Asciicast") 

## Configuration styles

The generated manifests can be customized either using annotations or configuration properties/yml.

### Using annotations

The `@KubernetesApplication` under `io.dekorate.kubernetes.annotation` is a `@Dekorate` alternative through which
the user can specify all sorts of customization, For example to set the replicas to 2:

```java
package org.acme;

import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(replicas=2)
public class Application {
}
```

### Using framework configuration

The same can be achieved using plain old configuration (e.g.`application.properties`):

```
dekorate.kubernetes.replicas=2
```

**A complete reference on all the supported properties can be found in the [configuration options guide](/assets/config.md).
