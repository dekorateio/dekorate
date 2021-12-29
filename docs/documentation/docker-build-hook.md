---
title: Docker Build Hook
description: Docker Build Hook
layout: docs
permalink: /docs/docker-build-hook
---

### Docker build hook
This hook will just trigger a docker build, using an existing Dockerfile at the root of the project.
It will not generate or customize the docker build in any way.

To enable the docker build hook you need:

- a `Dockerfile` in the project/module root
- the `docker` binary configured to point the docker daemon of your kubernetes environment.

To trigger the hook, you need to pass `-Ddekorate.build=true`  as an argument to the build, for example:
```bash
mvn clean install -Ddekorate.build=true
```
or if you are using gradle:
```bash
gradle build -Ddekorate.build=true   
```
When push is enabled, the registry can be specified as part of the annotation, or via system properties.
Here's an example via annotation configuration:
```java
@EnableDockerBuild(registry="quay.io")
public class Main {
}
```    
Here's how it can be done via build properties (system properties):
```bash
mvn clean install -Ddekorate.docker.registry=quay.io -Ddekorate.push=true    
```

Note: Dekorate will **NOT** push images on its own. It will delegate to the `docker` binary. So the user needs to make sure
beforehand they are logged in and have taken all necessary actions for a
`docker push` to work.
