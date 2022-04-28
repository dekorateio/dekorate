---
layout: docs
title: Get Started
permalink: /docs/get-started/
---

# Get started

## Using Spring Boot
The fast way to get started is to add the following dependency to your spring-boot application:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>2.9.3</version>
</dependency>
```

This should be enough to get dekorate to generate kubernets manifests under target/classes/META-INF/dekorate/.

You can now apply the generated manifests, from you module root:

kubectl apply -f target/classes/META-INF/dekorate/kubernetes.yml
or if your prefer to do it in a single step, right after the build:

mvn clean package -Ddekorate.deploy=true
This assume that an image for your application is already availalbe if not check building and deployg section.

Dekorating the manifests
The generated manifests, will try to adapt to your application. If for instance your application contains:

A REST controller
A router function
The generated manifests will be dekorated by exposing the http service.

If the actuator is detected, readiness and liveness probes pointing to the health endpoint will be added.

You can further dekorate the manifests, using the standard spring boot mechanisms (via application.properties or application.yml). Check the full list of configuration properties.

Building and deploying
To use the generated manifests, you need to create an image first.

On Kubernetes using docker
You can use a simple Dockerfile like:

```
FROM openjdk:8u171-alpine3.7
COPY target/*.jar kubernetes-example.jar
CMD java ${JAVA_OPTS} -jar kubernetes-example.jar
```

If such a Dockerfile is present in the module root it will be detected and used by dekorate, when specifying the option -Ddekorate.deploy=true.

On Openshift using binary builds
You will need to add dependency below as a supplement or replacement of kubernetes-spring-starter.

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-spring-starter</artifactId>
  <version>2.9.3</version>
</dependency>
```

When -Ddekorate.deploy=true is detected and openshift-spring-starter is available, an binary build will take place. Note: In this case, no Dockerfile is required.

Rebuilding an image
To rebuild the image, without reapllying the manifests, you can simply use:

```sh
mvn clean package -Ddekorate.build=true
```
Note: Both of these options `-Ddekorate.build=true` and `-Ddekorate.deploy=true` will use the corresponding binaries docker, oc and kubectl using a java shutdown hook.

### Hello Spring 

Checkout the [Hello Spring](./spring-boot) asciicast.

## Using the Java Framework of your choice

For generic java applications, you can use instead:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>2.9.3</version>
</dependency>
```

Then add `io.dekorate.annotation.Dekorate` to your any class on your project. Usually, people add that to the main class.

### Hello Generic java

Checkout the [Hello Generic Java / Vert.X](./generic-java-application) asciicast.

