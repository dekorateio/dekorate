---
title: Generic Java Application
description: Generic Java Application
layout: docs
permalink: /docs/generic-java-application
---

### Hello Generic Java Application

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
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

    mvn clean package

The generated manifests can be found under `target/classes/META-INF/dekorate`.


![asciicast](../images/dekorate-vertx-hello-world.gif "Dekorate Vert.X Hello World Asciicast")

#### related examples
- [vertx on kubernetes example](examples/vertx-on-kubernetes-example)
- [vertx on openshift example](examples/vertx-on-openshift-example)
