---
title: Junit-extensions
description: Junit-extensions
layout: docs
permalink: /docs/junit-extensions
---
### Junit5 extensions

Dekorate provides two junit5 extensions for:

- Kubernetes
- OpenShift

These extensions are `dekorate` aware and can read generated resources and configuration, in order to manage `end to end` tests
for the annotated applications.

#### Features

- Environment conditions
- Container builds
- Apply generated manifests to test environment
- Inject test with:
  - client
  - application pod

#### Kubernetes extension for JUnit5

The kubernetes extension can be used by adding the following dependency:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-junit</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```    
This dependency gives access to [@KubernetesIntegrationTest](testing/kubernetes-junit/src/main/java/io/dekorate/testing/annotation/KubernetesIntegrationTest.java) which is what enables the extension for your tests.

By adding the annotation to your test class the following things will happen:

1. The extension will check if a kubernetes cluster is available (if not tests will be skipped).
2. If `@EnableDockerBuild` is present in the project, a docker build will be triggered.
3. All generated manifests will be applied.
4. Will wait until applied resources are ready.
5. Dependencies will be injected (e.g. KubernetesClient, Pod etc)
6. Test will run
7. Applied resources will be removed.

##### Dependency injection

Supported items for injection:

- KubernetesClient
- Pod (the application pod)
- KubernetesList (the list with all generated resources)

To inject one of this you need a field in the code annotated with [@Inject](testing/core-junit/src/main/java/io/dekorate/testing/annotation/Inject.java).

For example:
```java
@Inject
KubernetesClient client;
```    
When injecting a Pod, it's likely we need to specify the pod name. Since the pod name is not known in advance, we can use the deployment name instead.
If the deployment is named `hello-world` then you can do something like:
```java
@Inject
@Named("hello-world")
Pod pod;
```
Note: It is highly recommended to also add `maven-failsafe-plugin` configuration so that integration tests only run in the `integration-test` phase.
This is important since in the `test` phase the application is not packaged. Here's an example of how it you can configure the project:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-failsafe-plugin</artifactId>
  <version>${version.maven-failsafe-plugin}</version>
  <executions>
    <execution>
      <goals>
        <goal>integration-test</goal>
        <goal>verify</goal>
      </goals>
      <phase>integration-test</phase>
      <configuration>
        <includes>
          <include>**/*IT.class</include>
        </includes>
      </configuration>
    </execution>
  </executions>
</plugin>
```

#### related examples
- [spring boot on kubernetes example](examples/spring-boot-on-kubernetes-example)

#### OpenShift extension for JUnit5

Similarly, to using the [kubernetes junit extension](#kubernetes-extension-for-junit5) you can use the extension for OpenShift, by adding  [@OpenshiftIntegrationTest](testing/openshift-junit/src/main/java/io/dekorate/testing/openshift/annotation/OpenshiftIntegrationTest.java).
To use that you need to add:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-junit</artifactId>
  <version>2.7.0</version>
</dependency>
```    
By adding the annotation to your test class the following things will happen:

1. The extension will check if a kubernetes cluster is available (if not tests will be skipped).
2. A docker build will be triggered.
3. All generated manifests will be applied.
4. Will wait until applied resources are ready.
5. Dependencies will be injected (e.g. KubernetesClient, Pod etc)
6. Test will run
7. Applied resources will be removed.

#### related examples
- [spring boot on openshift example](examples/spring-boot-on-openshift-example)
- [spring boot with groovy on openshift example](examples/spring-boot-with-groovy-on-openshift-example)
- [spring boot with gradle on openshift example](examples/spring-boot-with-gradle-on-openshift-example)

#### Configuration externalization
It is often desired to externalize configuration in configuration files, instead of hard coding things inside annotations.

Dekorate provides the ability to externalize configuration to configuration files (properties or yml).
This can be done to either override the configuration values provided by annotations, or to use dekorate without annotations.

For supported frameworks, this is done out of the box, as long as the corresponding framework jar is present.
The frameworks supporting this feature are:

- spring boot
- thorntail

For these frameworks, the use of annotations is optional, as everything may be configured via configuration files.
Each annotation may be expressed using properties or yaml using the following steps.

- Each annotation property is expressed using a key/value pair.
- All keys start with the `dekorate.<annotation kind>.` prefix, where `annotation kind` is the annotation class name in lowercase, stripped of the `Application` suffix.
- The remaining part of key is the annotation property name.
- For nesting properties the key is also nested following the previous rule.

For all other frameworks or generic java application this can be done with the use of the `@Dekorate` annotation.
The presence of this annotation will trigger the dekorate processes. Dekorate will then look for `application.properites` or `application.yml` resources.
If present, they will be loaded. If not the default configuration will be used.


Examples:

The following annotation configuration:

    @KubernetesApplication(labels=@Label(key="foo", value="bar"))
    public class Main {
    }

Can be expressed using properties:

    dekorate.kubernetes.labels[0].key=foo
    dekorate.kubernetes.labels[0].value=bar

or using yaml:

    dekorate:
      kubernetes:
        labels:
          - key: foo
            value: bar


In the examples above, `dekorate` is the prefix that we use to `namespace` the dekorate configuration. `kubernetes` defines the annotation kind (its `@KubernetesApplication` in lower case and stripped of the `Application` suffix).
`labels`, `key` and `value` are the property names and since the `Label` is nested under `@KubernetesApplication` so are the properties.

The exact same example for OpenShift (where `@OpenshiftApplication` is used instead) would be:

    @OpenshiftApplication(labels=@Label(key="foo", value="bar"))
    public class Main {
    }

Can be expressed using properties:

    dekorate.openshift.labels[0].key=foo
    dekorate.openshift.labels[0].value=bar

or using yaml:

    dekorate:
      openshift:
        labels:
          - key: foo
            value: bar


##### Spring Boot

For spring boot, dekorate will look for configuration under:

- application.properties
- application.yml
- application.yaml

Also, it will look for the same files under the kubernetes profile:

- application-kubernetes.properties
- application-kubernetes.yml
- application-kubernetes.yaml

##### Vert.x & generic Java

For generic java, if the @Dekorate annotation is present, then dekorate will
look for confiugration under:

- application.properties
- application.yml

These files can be overridden using the `configFiles` property on the `@Dekorate` annotation.

For example:

A generic java application annotated with `@Dekorate`:

```java

    import io.dekorate.annotation.Dekorate;
    
    @Dekorate
    public class Main {
        //do stuff
    }
```

During compilation kubernetes, OpenShift or both resources will be generated (depending on what dekorate jars are present in the classpath).
These resources can be customized using properties:

    dekorate.openshift.labels[0].key=foo
    dekorate.openshift.labels[0].value=bar

or using yaml:

    dekorate:
      openshift:
        labels:
          - key: foo
            value: bar

#### related examples
- [Vert.x on kubernetes example](examples/vertx-on-kubernetes-example)
- [Vert.x on openshift example](examples/vertx-on-openshift-example)

#### Testing Multi-Module projects

The Dekorate testing framework supports multi-module projects either using [the OpenShift JUnit 5 extension](#openshift-extension-for-junit5) or using [the Kubernetes JUnit 5 extension](#kubernetes-extension-for-junit5).

A multi-module project consist of multiple modules, all using Dekorate to generate the cluster manifests and a `tests` module that will run the integration tests:

```
multi-module-parent
└───module-1
└───module-2
└───tests
```

In the `tests` module, we can now specify the location of the additional modules via the field `additionalModules` which is part of the `@OpenshiftIntegrationTest` and `@KubernetesIntegrationTest` annotations:

```java
@OpenshiftIntegrationTest(additionalModules = { "../module-1", "../module-2" })
class SpringBootForMultipleAppsOnOpenshiftIT {

  @Inject
  private KubernetesClient client;

  @Inject
  @Named("module-1")
  Pod podForModuleOne;

  @Inject
  @Named("module-2")
  Pod podForModuleTwo;

  // ...
}
```

Doing so, the test framework will locate the Dekorate manifests that have been previously generated to build and deploy the application for each integration test.

##### related examples
- [Multi-Module projects on OpenShift example](examples/multimodule-projects-on-openshift-example)
- [Multi-Module projects on Kubernetes example](examples/multimodule-projects-on-kubernetes-example)
