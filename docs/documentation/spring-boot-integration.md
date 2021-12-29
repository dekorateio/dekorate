---
title: Spring Boot integration
description:  Spring Boot integration
layout: docs
permalink: /docs/spring-boot-integration
---

#### Spring Boot

With spring boot, we suggest you start with one of the provided starters:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

Or if you are on [OpenShift](https://openshift.com):

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshfit-spring-starter</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

##### Automatic configuration

For Spring Boot application, dekorate will automatically detect known annotation and will align generated manifests accordingly.

##### Exposing services

Dekorate tunes the generated manifest based on the presence of web annotations in the project:

- Automatic service expose
- Application path detection

When known web annotations are available in the project, dekorate will automatically detect and expose the http port as a Service.
That service will also be expose as an `Ingress` or `Route` (in case of Openshift) if the `expose` option is set to true.

###### Kubernetes
```java
@KubernetesApplication(expose=true)
```

An alternative way of configuration is via `application properties`:

```
dekorate.kubernetes.expose=true
```

###### Openshift
```java
@OpenshiftApplication(expose=true)
```

An alternative way of configuration is via `application properties`:

```
dekorate.openshift.expose=true
```

There are cases where the `Ingress` or `Route` host needs to be customized. This is done using the `host` parametes either via annotation or property configuration.


###### Kubernetes
```java
@KubernetesApplication(expose=true, host="foo.bar.com")
```

An alternative way of configuration is via `application properties`:

```
dekorate.kubernetes.expose=true
dekorate.kubernetes.host=foo.bar.com
```

###### Openshift
```java
@OpenshiftApplication(expose=true, host="foo.bar.com")
```

An alternative way of configuration is via `application properties`:

```
dekorate.openshift.expose=true
dekorate.openshift.host=foo.bar.com
```


###### RequestMapping

When one `RequestMapping` annotation is added on a `Controller` or multiple `RequestMapping` that share a common path are added on multiple `Controller` classes,
dekorate will detect the shortest common path and configure it so that its available on the expose `Ingress` or `Route`.
