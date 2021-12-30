---
title: Spring Boot
description: Spring Boot
layout: docs
permalink: /docs/spring-boot
---

### Hello Spring Boot

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

That's all! Next time you perform a build, using something like:

    mvn clean package

The generated manifests can be found under `target/classes/META-INF/dekorate`.

![asciicast](../images/dekorate-spring-hello-world.gif "Dekorate Spring Boot Hello World Asciicast")

#### related examples
- [spring boot on kubernetes example](https://github.com/dekorateio/dekorate/tree/main/examples/spring-boot-on-kubernetes-example)
- [spring boot on openshift example](https://github.com/dekorateio/dekorate/tree/main/examples/spring-boot-on-openshift-example)
