---
title: Framework integration
description:  Framework integration
layout: docs
permalink: /docs/framework-integration
---

###  Framework integration

Framework integration modules are provided that we are able to detect framework annotations and adapt to the framework (e.g. expose ports).

The frameworks supported so far:

- Spring Boot
- Quarkus

#### Spring Boot

With spring boot, we suggest you start with one of the provided starters:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>{{site.release.version}}</version>
</dependency>
```

Or if you are on [OpenShift](https://openshift.com):

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshfit-spring-starter</artifactId>
  <version>{{site.release.version}}</version>
</dependency>
```

#### Automatic configuration

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

There are cases where the `Ingress` or `Route` host needs to be customized. This is done using the `host` parameter either via annotation or property configuration.


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

#### Annotation less configuration

It is possible to completely bypass annotations by utilizing already-existing, framework-specific metadata. This mode is
currently only supported for Spring Boot applications (i.e. at least one project class is annotated with `@SpringBootApplication`).

So, for Spring Boot applications, all you need to do is add one of the starters (`io.dekorate:kubernetes-spring-starter` or
`io.dekorate:openshift-spring-starter`) to the classpath. No need to specify an additional annotation.
This provides the fastest way to get started using [dekorate](https://github.com/dekorateio/dekorate) with [Spring Boot](https://spring.io/projects/spring-boot).

To customize the generated manifests you can add `dekorate` properties to your `application.yml` or `application.properties`
descriptors, or even use annotations along with `application.yml` / `application.properties` though if you define `dekorate`
properties then the annotation configuration will be replaced by the one specified using properties.

Dekorate looks for supported configuration as follows in increasing order of priority, meaning any configuration found in
an `application` descriptor will override any existing annotation-specified configuration:

1. Annotations
2. `application.properties`
3. `application.yaml`
4. `application.yml`

Then,  it will use the properties file depending on the active Dekorate dependencies in use. For example, if we're using the dependency `io.dekorate:kubernetes-annotations`, then:
1. `application-kubernetes.properties`
2. `application-kubernetes.yaml`
3. `application-kubernetes.yml`

| Note that only the `openshift`, `kubernetes` and `knative` modules are providing additional properties files.

Then, for Spring Boot applications, it will also take into account the Spring property `spring.profiles.active` if set:
1. `application-${spring.profiles.active}.properties`
2. `application-${spring.profiles.active}.yaml`
3. `application-${spring.profiles.active}.yml`

Finally, if the Dekorate profile property `dekorate.properties.profile` is set:
1. if property `dekorate.properties.profile` is set, then `application-${dekorate.properties.profile}.properties`
2. if property `dekorate.properties.profile` is set, then `application-${dekorate.properties.profile}.yaml`
3. if property `dekorate.properties.profile` is set, then `application-${dekorate.properties.profile}.yml`

It's important to repeat that the override that occurs by *fully* replacing any lower-priority configuration and not via any kind
of merge between the existing and higher-priority values. This means that if you choose to override the annotation-specified
configuration, you need to repeat all the configuration you want in the @Env annotation-less configuration.

Here's the full list of supported [configuration options](assets/config.md). Special attention should be paid to the path of these
properties. The properties' path match the annotation properties and not what would end up in the manifest, meaning the
annotation-less configuration matches the model defined by the annotations. More precisely, what is being configured using
properties is the same model as what is configured using annotations. While
there is some overlap between how the annotations
are configured and the resulting manifest, the properties (or YAML file) still need to provide values for the annotation fields,
hence why they need to match how the annotations are configured. Always refer to the [configuration options guide](assets/config.md)
if in doubt.

###### Generated resources when not using annotations

When no annotations are used, the kind of resources to be generated is determined by the `dekorate` artifacts found in the classpath.

| File                | Required Dependency                |
|---------------------|------------------------------------|
| kubernetes.json/yml | io.dekorate:kubernetes-annotations |
| openshift.json/yml  | io.dekorate:openshift-annotations  |


Note: that starter modules for `kubernetes` and `openshift` do transitively add `kubernetes-annotations` and `openshift-annotations` respectively.

#### Quarkus

[quarkus](https://quarkus.io) provides rich set of [extensions](https://quarkus.io/extensions) including one for [kubernetes](https://quarkus.io/guides/deploying-to-kubernetes).
The [kubernetes extension](https://quarkus.io/guides/deploying-to-kubernetes) uses internally [dekorate](https://github.com/dekorateio/dekorate) for generating and customizing manifests.

The extension can be added to any [quarkus](https://quarkus.io) project:

    mvn quarkus:add-extension -Dextensions="io.quarkus:quarkus-kubernetes"

After the project compilation the generated manifests will be available under: `target/kubernetes/`.

At the moment this extension will handle ports, health checks etc, with zero configuration from the user side.

It's important to note, that by design this extension will NOT use the [dekorate](https://github.com/dekorateio/dekorate) annotations for customizing the generated manifests.

For more information please check: the extension [docs](https://quarkus.io/guides/deploying-to-kubernetes).

