---
title: Annotationless configuration
description:  Annotationless configuration
layout: docs
permalink: /docs/annotation-less-configuration
---

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
5. `application-kubernetes.properties`
6. `application-kubernetes.yaml`
7. `application-kubernetes.yml`

It's important to repeat that the override that occurs by *fully* replacing any lower-priority configuration and not via any kind
of merge between the existing and higher-priority values. This means that if you choose to override the annotation-specified
configuration, you need to repeat all the configuration you want in the @Env annotation-less configuration.

Here's the full list of supported [configuration options]({{site.baseurl}}/configuration-guide). Special attention should be paid to the path of these
properties. The properties' path match the annotation properties and not what would end up in the manifest, meaning the
annotation-less configuration matches the model defined by the annotations. More precisely, what is being configured using
properties is the same model as what is configured using annotations. While
there is some overlap between how the annotations
are configured and the resulting manifest, the properties (or YAML file) still need to provide values for the annotation fields,
hence why they need to match how the annotations are configured. Always refer to the [configuration options guide]({{site.baseurl}}/configuration-guide)
if in doubt.

###### Generated resources when not using annotations

When no annotations are used, the kind of resources to be generated is determined by the `dekorate` artifacts found in the classpath.

| File                | Required Dependency                |
|---------------------|------------------------------------|
| kubernetes.json/yml | io.dekorate:kubernetes-annotations |
| openshift.json/yml  | io.dekorate:openshift-annotations  |


Note: that starter modules for `kubernetes` and `openshift` do transitively add `kubernetes-annotations` and `openshift-annotations` respectively.
