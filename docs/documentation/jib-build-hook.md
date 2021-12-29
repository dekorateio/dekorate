---
title: JIB Build Hook
description: JIB Build Hook
layout: docs
permalink: /docs/jib-build-hook
---

### Jib build hook
This hook will just trigger a jib build in order to perform a container build.

In order to use it, one needs to add the `jib-annotations` dependency.

```xml
<dependencies>
  <groupId>io.dekorate</groupId>
  <artifactId>jib-annotations</artifactId>
</dependencies>
```

Without the need of any additional configuration, one trigger the hook by passing `-Ddekorate.build=true`  as an argument to the build, for example:

```bash
mvn clean install -Ddekorate.build=true
```
or if you are using gradle:
```bash
gradle build -Ddekorate.build=true
``` 

##### Jib modes

At the moment Jib allows you to create and push images in two different ways:

- using the docker daemon
- dockerless

At the moment performing a build through the docker daemon is slightly safer, and thus is used as a default option.
You can easily switch to dockerless mode, by setting the `@JibBuild(dockerBuild=false)` or if using properties configuration `dekorate.jib.docker-build=false`.

In case of the dockerless mode, an `openjdk-8` image is going to be used as a base image. The image can be changed through the `from` property on the @JibBuild annotation or `dekorate.jib.from` when using property configuration.

#### related examples
- [spring boot on kubernetes with jib example](examples/spring-boot-on-kubernetes-with-jib-example)
