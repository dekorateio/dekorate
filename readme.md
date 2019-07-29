![dekorate logo](logo.png "Dekorate") 

# Dekorate

[![CircleCI](https://circleci.com/gh/dekorateio/dekorate.svg?style=svg)](https://circleci.com/gh/dekorateio/dekorate) [![Maven Central](https://img.shields.io/maven-central/v/io.dekorate/kubernetes-annotations.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.dekorate%22%20AND%20a:%22kubernetes-annotations%22)

Dekorate is a collection of Java compile-time generators and decorators for Kubernetes/OpenShift manifests.

It makes generating Kubernetes manifests as easy as adding a dependency to the classpath and customizing as simple as setting an annotation or application property.

Stop wasting time editing xml, json and yml and customize the kubernetes manifests as you configure your java application.

## Rebranding Notice

This project was originally called `ap4k` which stood for `Annotation Processors for Kubernetes`.
As the project now supports `decorating` of kubernetes manifests without the use of annotations, the name `ap4k` no longer describes the project in the best possible way. So, the project has been renamed to `dekorate`.

The new project page will be: http://dekorate.io and the new repository will be: https://github.com/dekorateio/dekorate.
This repository will stay open until all remaining pull requests are processed and then  it will redirect to  https://github.com/dekorateio/dekorate.

For new issues and pull requests please don't forget to use https://github.com/dekorateio/dekorate.

## Features

- Generates manifest via annotation processing
  - [Kubernetes](#kubernetes-annotations)
  - [OpenShift](#openshift-annotations)
  - [Prometheus](#prometheus-annotations)
  - [Jaeger](#jaeger-annotations)
  - [Service Catalog](#service-catalog-annotations)
  - [Component CRD](#component-crd-support)
- Customize manifests using annotations
  - Kubernetes
    - labels
    - annotations
    - [environment variables](#adding-container-environment-variables)
    - [mounts](#working-with-volumes-and-mounts)
    - [ports and services](#adding-extra-ports-and-exposing-them-as-services)
    - [jvm options](#jvm-options)
    - [init containers](#init-containers)
    - [sidecars](#sidecars)
  - OpenShift 
    - [image streams](#integrating-with-s2i)
    - build configurations
  - Prometheus
  - Service Catalog
    - service instances
    - inject bindings into pods
  - Component CRD
- Build tool independent (works with maven, gradle, bazel and so on)
- [Rich framework integration](#framework-integration)
  - Port, Service and Probe auto configuration
    - Spring Boot
- [Configuration externalization for known frameworks](#configuration-externalization-for-known-frameworks) (annotationless)
  - Spring Boot
- Integration with external generators
- [Rich set of examples](examples)

### Experimental features

- Register hooks for triggering builds and deployment
  - Build hooks
    - [Docker build hook](#docker-build-hook)
    - Source to image build hook
- junit5 integration testing extension
  - [Kubernetes](#kubernetes-extension-for-junit5)
  - [OpenShift](#openshift-extension-for-juni5)

## Rationale

The are tons of tools out there for scaffolding / generating kubernetes manifests. Sooner or later these manifests will require customization.
Handcrafting is not an appealing option.
Using external tools, is often too generic.
Using build tool extensions and adding configuration via xml, groovy etc is a step forward, but still not optimal.

Annotation processing has quite a few advantages over external tools or build tool extensions:

- Configuration is validated by the compiler.
- Leverages tools like the IDE for writing type safe config (checking, completion etc).
- Works with all build tools.
- Can "react" to annotations provided by the framework.

## Usage

To start using this project you just need to add one of the provided annotations to your project.

### Kubernetes annotations

[@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) can be added to your project like:

```java
import io.dekorate.kubernetes.annotaion.KubernetesApplication;

@KubernetesApplication
public class Main {

    public static void main(String[] args) {
      //Your application code goes here.
    }
}
```

When the project gets compiled, the annotation will trigger the generation of a `Deployment` in both json and yml that
will end up under 'target/classes/META-INF/dekorate'. 

The annotation comes with a lot of parameters, which can be used in order to customize the `Deployment` and/or trigger
the generations of addition resources, like `Service` and `Ingress`.

#### Adding the kubernetes annotation processor to the classpath

This module can be added to the project using:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

#### related examples
 - [kubernetes example](examples/kubernetes-example)


#### Name and Version

So where did the generated `Deployment` gets its name, docker image etc from?

Everything can be customized via annotation parameters and system properties.
On top of that `lightweight` integration with build tools is provided in order to reduce duplication.

##### Lightweight build tool integration

Lightweight integration with build tools, refers to reading information from the build tool config without bringing in the build tool itself into the classpath.
The information read from the build tool is limited to:

- name / artifactId
- version
- output file

For example in the case of maven it refers to parsing the pom.xml with DOM in order to fetch the artifactId and version.

Supported build tools:

- maven
- gradle
- sbt
- bazel

For all other build tools, the name and version need to be provided via the core annotations:

```java
@KubernetesApplication(name = "my-app", version="1.1.0.Final")
public class Main {
}
```
     
or

```java
@OpenshiftApplication(name = "my-app", version="1.1.0.Final")
public class Main {
}
```
and so on...

The information read from the build tool, is added to all resources as labels (name, version).
They are also used to name images, containers, deployments, services etc.

For example for a gradle app, with the following `gradle.properties`:
```properties
name = my-gradle-app
version = 1.0.0
```

The following deployment will be generated:
```yaml
apiVersion: "apps/v1"
kind: "Deployment"
metadata:
  name: "kubernetes-example"
spec:
  replicas: 1
  selector:
    matchLabels:
      app: "my-gradle-app"
      version: "1.0-SNAPSHOT"
      group: "default"
  template:
    metadata:
      labels:
        app: "my-gradle-app"
        version: "1.0-SNAPSHOT"
        group: "default"
    spec:
      containers:
      - env:
        - name: "KUBERNETES_NAMESPACE"
          valueFrom:
            fieldRef:
              fieldPath: "metadata.namespace"
        image: "default/my-gradle-app:1.0-SNAPSHOT"
        imagePullPolicy: "IfNotPresent"
        name: "my-gradle-app"
```            
The output file name may be used in certain cases, to set the value of `JAVA_APP_JAR` an environment variable that points to the build jar.



#### Adding extra ports and exposing them as services

To add extra ports to the container, you can add one or more `@Port` into your  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) :
```java
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(ports = @Port(name = "web", containerPort = 8080))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

This will trigger the addition of a container port to the `Deployment` but also will trigger the generation of a `Service` resource.

**Note:**  This doesn't need to be done explicitly, if the application framework is detected and support, ports can be extracted from there *(see below)*.

#### Adding container environment variables
To add extra environment variables to the container, you can add one or more `@EnvVar` into your  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) :
```java
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(envVars = @Env(name = "key1", value = "var1"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

Additional options are provided for adding environment variables from fields, config maps and secrets.    

#### Working with volumes and mounts
To define volumes and mounts for your application, you can use something like:
```java
import io.dekorate.kubernetes.annotation.Port;
import io.dekorate.kubernetes.annotation.Mount;
import io.dekorate.kubernetes.annotation.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(ports = @Port(name = "http", containerPort = 8080), 
  pvcVolumes = @PersistentVolumeClaimVolume(volumeName = "mysql-volume", claimName = "mysql-pvc"),
  mounts = @Mount(name = "mysql-volume", path = "/var/lib/mysql")
)
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```    
    
Currently the supported annotations for specifying volumes are:

- @PersistentVolumeClaimVolume
- @SecretVolume
- @ConfigMapVolume
- @AwsElasticBlockStoreVolume
- @AzureDiskVolume
- @AzureFileVolume
   
#### Jvm Options
It's common to pass the JVM options in the manifests using the `JAVA_OPTS` environment variable of the application container.
This is something complex as it usually difficult to remember all options by heart and thus its error prone.
The worst part is that you don't realize the mistake until its TOO late.

Dekorate provides a way to manage those options using the `@JvmOption` annotation, which is included in the `options-annotations`.

```java
import io.dekorate.options.annotation.JvmOptions
import io.dekorate.options.annotation.GarbageCollector;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication
@JvmOptions(server=true, xmx=1024, preferIpv4Stack=true, gc=GarbageCollector.SerialGC)
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>option-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

Note: The module is included in all starters.    
    
#### Init Containers

If for any reason the application requires the use of init containers, they can be easily defined using the `initContainer`
property, as demonstrated below.
```java
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(initContainers = @Container(image="foo/bar:latest", command="foo"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

The [@Container](core/src/main/java/io/dekorate/kubernetes/annotation/Container.java) supports the following fields:

- Image
- Image Pull Policy
- Commands
- Arguments
- Environment Variables
- Mounts
- Probes

#### Sidecars

Similarly to [init containers](#init-containers) support for sidecars is also provided using the `sidecars` property. For example:
```java
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(sidecars = @Container(image="jaegertracing/jaeger-agent",
                                             args="--collector.host-port=jaeger-collector.jaeger-infra.svc:14267"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

As in the case of [init containers](#init-containers) the [@Container](core/src/main/java/io/dekorate/kubernetes/annotation/Container.java) supports the following fields:

- Image
- Image Pull Policy
- Commands
- Arguments
- Environment Variables
- Mounts
- Probes

#### Adding the kubernetes annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```
### OpenShift annotations

This module provides two new annotations: 

- @OpenshiftApplication

[@OpenshiftApplication](annotations/openshift-annotations/src/main/java/io/dekorate/openshift/annotation/OpenshiftApplication.java) works exactly like  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) , but will generate resources in a file name `openshift.yml` / `openshift.json` instead.
Also instead of creating a `Deployment` it will create a `DeploymentConfig`.

**NOTE:** A project can use both [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) and [@OpenshiftApplication](annotations/openshift-annotations/src/main/java/io/dekorate/openshift/annotation/OpenshiftApplication.java). If both the kubernetes and
openshift annotation processors are present both kubernetes and openshift resources will be generated. 

#### Adding the OpenShift annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```    
#### Integrating with S2i
Out of the box resources for s2i will be generated.

- ImageStream
  - builder 
  - target
- BuildConfig 

Here's an example:
```java
import io.dekorate.openshift.annotation.OpenshiftApplication;

@OpenshiftApplication(name = "doc-example")
public class Main {

    public static void main(String[] args) {
      //Your code goes here
    }
}
```    
The generated `BuildConfig` will be a binary config. The actual build can be triggered from the command line with something like:

    oc start-build doc-example --from-dir=./target --follow

**NOTE:** In the example above we explicitly set a name for our application, and we refernced that name from the cli. 
If the name was implicitly created the user would have to figure the name out before triggering the build. This could be
done either by `oc get bc` or by knowing the conventions used to read names from build tool config (e.g. if maven then name the artifactId).

#### related examples

- [openshift example](examples/openshift-example)
- [source to image example](examples/source-to-image-example)
- [spring boot on openshift example](examples/spring-boot-on-openshift-example)
- [spring boot with groovy on openshift example](examples/spring-boot-with-groovy-on-openshift-example)
- [spring boot with gradle on openshift example](examples/spring-boot-with-gradle-on-openshift-example) 

### Prometheus annotations

The [prometheus](https://prometheus.io/) annotation processor provides annotations for generating prometheus related resources.
In particular it can generate [ServiceMonitor](annotations/prometheus-annotations/src/main/java/io/dekorate/prometheus/model/ServiceMonitor.java) which are used by the
[Prometheus Operator](https://github.com/coreos/prometheus-operator) in order to configure [prometheus](https://prometheus.io/) to collect metrics from the target application.

This is done with the use of [@EnableServiceMonitor](annotations/prometheus-annotations/src/main/java/io/dekorate/prometheus/annotation/EnableServiceMonitor.java) annotation.

Here's an example:
```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.prometheus.annotation.EnableServiceMonitor;

@KubernetesApplication
@EnableServiceMonitor(port = "http", path="/prometheus", interval=20)
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```
The annotation processor, will automatically configure the required selector and generate the ServiceMonitor.
Note: Some of the framework integration modules, may further decorate the ServiceMonitor with framework specific configuration.
For example, the Spring Boot module will decorate the monitor with the Spring Boot specific path, which is `/actuator/prometheus`.

#### related examples
- [spring boot with prometheus on kubernetes example](examples/spring-boot-with-prometheus-on-kubernetes-example)

### Jaeger annotations

The [jaeger](https://www.jaegertracing.io) annotation processor provides annotations for injecting the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent) into the application pod.

Most of the work is done with the use of the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/dekorate/jaeger/annotation/EnableJaegerAgent.java) annotation.

#### Using the Jaeger Operator

When the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) is available, you set the `operatorEnabled` property to `true`.
The annotation processor will automicatlly set the required annotations to the generated deployment, so that the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) can inject the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent).

Here's an example:
```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.jaeger.annotation.EnableJaegerAgent;

@KubernetesApplication
@EnableJaegerAgent(operatorEnabled="true")
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```    
##### Manually injection the agent sidecar

For the cases, where the operator is not present, you can use the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/dekorate/jaeger/annotation/EnableJaegerAgent.java) to manually configure the sidecar.

```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.jaeger.annotation.EnableJaegerAgent;

@KubernetesApplication
@EnableJaegerAgent
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```
#### related examples
- [spring boot with jaeger on kubernetes example](examples/spring-boot-with-jeager-on-kubernetes-example)

### Service Catalog annotations
The [service catalog](https://svc-cat.io) annotation processor is can be used in order to create [service catalog](https://svc-cat.io) resources for:

- creating service instances
- binding to services
- injecting binding info into the container 

Here's an example:
```java
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.servicecatalog.annotation.ServiceCatalogInstance;
import io.dekorate.servicecatalog.annotation.ServiceCatalog;

@KubernetesApplication
@ServiceCatalog(instances =
    @ServiceCatalogInstance(name = "mysql-instance", serviceClass = "apb-mysql", servicePlan = "default")
)
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```
The `@ServiceCatalogInstance` annotation will trigger the generation of a `ServiceInstance` and a `ServiceBinding`resource.
It will also decorate any `Pod`, `Deployment`, `DeploymentConfig` and so on with additional environment variables containing the binding information.

#### Adding the service catalog annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>servicecatalog-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

#### related examples
 - [service catalog example](examples/service-catalog-example)  
 
### Component CRD support
The Component [Custom Resource Definition (CRD)](https://kubernetes.io/docs/tasks/access-kubernetes-api/custom-resources/custom-resource-definitions/) aims 
to abstract kubernetes/OpenShift resources and simplify the configuration and design of cloud-native applications.
See the following [project](https://github.com/snowdrop/component-operator) to get more information about the Component CRD and
its associated operator, more specifically you can take a look at the 
[demo project](https://github.com/snowdrop/component-operator/tree/master/demo).
This module provides support for the Component CRD.

The generation of a Component CRD descriptor is triggered by adding the `component-annotations` dependency to the project and
annotate one of your classes with `@ComponentApplication`. Note that in the case of Spring Boot applications, as explained 
[here](#annotation-less), only adding the dependency is needed:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>component-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

If everything went well, building your project will also generate `component.yml` and `component.json` files in the 
`target/classes/META-INF/dekorate` is triggered.

The content of the component descriptor will be determined by the existing config provided by other annotations such as 
`@KubernetesApplication` and can be also controlled using application properties.

###  Framework integration

Framework integration modules are provided that we are able to detect framework annotations and adapt to the framework (e.g. expose ports).

The frameworks supported so far: 

- Spring Boot

#### Spring Boot

With spring boot its suggested to start with one of the provided starters:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

Or if you are on [openshift](https://openshift.com):

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshfit-spring-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

##### Annotation less
For Spring Boot applications (i.e. at least one project class is annotated with `@SpringBootApplication`) all you need to do, is adding one of the starters (`io.dekorate:kubernetes-spring-starter` or `io.dekorate:openshift-spring-starter`) to the classpath. No need to specify an additional annotation.
This provides the fastest way to get started using [dekorate](https://github.com/dekorateio/dekorate) with [spring boot](https://spring.io/projects/spring-boot).

To customize/dekorate the generated manifests you can use `application.yml` / `application.properties` both, or even use annotations along with  `application.yml` / `application.properties`.

The order where these resources are used are:

1. Annotations
2. `application.properties`
3. `application.yaml`
4. `application.yml`
5. `application-kubernetes.properties`
6. `application-kubernetes.yaml`
7. `application-kubernetes.yml`

Here's the full list of supported [configuration options](config.md).


###### Generated resources when not using annotations

When no annotations are used, the kind of resources to be generated is determined by the `dekorate` artifacts found in the classpath.

| File                | Required Dependency                |
|---------------------|------------------------------------|
| kubernetes.json/yml | io.dekorate:kubernetes-annotations |
| openshift.json/yml  | io.dekorate:openshift-annotations  |
| component.json/yml  | io.dekorate:component-annotations  |


Note: that starter modules for `kubernetes` and `openshift` do transitively add `kubernetes-annotations` and `openshift-annotations` respectively.

## Experimental features

Apart from the core feature, which is resource generation, there are a couple of experimental features that do add to the developer experience.

These features have to do with things like building, deploying and testing.

### Building and Deploying?
Dekorate does not generate Dockerfiles, neither it provides internal support for performing docker or s2i builds.
It does however allow the user to hook external tools (e.g. the `docker` or `oc`) to trigger container image builds after the end of compilation.

So, at the moment as an experimental feature the following hooks are provided:

- docker build hook (requires docker binary, triggered with `-Ddekorate.build=true`)
- docker push hook (requires docker binary, triggered with `-Ddekorate.push=true`)
- openshift s2i build hook (requires oc binary, triggered with `-Ddekorate.deploy=true`)

#### Docker build hook
This hook will just trigger a docker build, using an existing Dockerfile at the root of the project.
It will not generate or customize the docker build in anyway.

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
And here's how it can be done via build properties (system properties):
```bash
mvn clean install -Ddekorate.docker.registry=quay.io -Ddekorate.push=true    
```

Note: Dekorate will **NOT** push images on its own. It will delegate to the `docker` binary. So the user needs to make sure
beforehand that is logged in and has taken all necessary actions for a `docker push` to work.
    
#### S2i build hook
This hook will just trigger an s2i binary build, that will pass the output folder as an input to the build

To enable the docker build hook you need:

- the `openshift-annotations` module (already included in all openshift starter modules)
- the `oc` binary configured to point the docker daemon of your kubernetes environment.

Finally, to trigger the hook, you need to pass `-Ddekorate.build=true`  as an argument to the build, for example:
```bash
mvn clean install -Ddekorate.build=true
```   
or if you are using gradle:
```bash
gradle build -Ddekorate.build=true  
```    
### Junit5 extensions 

Dekorate provides two junit5 extensions for:

- Kubernetes
- Openshift

These extensions are `dekorate` aware and can read generated resources and configuration, in order to manage `end to end` tests
for the annotated applications.

#### Features

- Environment conditions
- Container builds
- Apply generated manifests to test environment
- Inject test with:
   - client
   - application pod

#### Kubernetes extension for Junit5

The kubernetes extension can be used by adding the following dependency:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-junit</artifactId>
  <version>${project.version}</version>
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
When injecting a Pod, its likely that we need to specify the pod name. Since the pod name is not known in advance, we can use the deployment name instead.
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

Similarly to using the [kubernetes junit extension](#kubernetes-extension-for-junit5) you can use the extension for OpenShift, by adding  [@OpenshiftIntegrationTest](testing/openshift-junit/src/main/java/io/dekorate/testing/annotation/OpenshiftIntegrationTest.java).
To use that you need to add:
```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-junit</artifactId>
  <version>${project.version}</version>
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
 - [spring boot with groovy on openshift example](examples/spring-boot-with-groovy-openshift-example)
 - [spring boot with gradle on openshift example](examples/spring-boot-with-gradle-openshift-example)
 
#### Configuration externalization for known frameworks
It is often desired to externalize configuration in configuration files, instead of hard coding things inside annotations.

Dekorate is graudally adding support for configuration externalization for the supported frameworks:

- spring boot

For these frameworks, the use of annotations is optional, as everything may be configured via configuration files.
Each annotation may be expressed using properties or yaml using the following steps.

- Each annotation property is expressed using a key/value pair.
- All keys start with the `dekorate.<annotation kind>.` prefix, where `annotation kind` is the annotation class name in lowercase, stripped of the `Application` suffix.
- The remaining part of key is the annotation property name.
- For nesting properties the key is also nested following the previous rule.

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

The exact same example for openshift (where `@OpenshiftApplication` is used instead) would be:

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

Also it will look for the same files under the kubernetes profile:

- application-kubernetes.properties
- application-kubernetes.yml
- application-kubernetes.yaml


#### External generator integration

No matter how good a generator/scaffolding tool is, its often desirable to handcraft part of it.
Other times it might be desirable to combine different tools together (e.g. to generate the manifests using fmp but customize them via dekorate annotations)

No matter what the reason is, dekorate supports working on existing resources and decorating them based on the provided annotation configuration.
This is as simple as letting dekorate know where to read the existing manifests and where to store the generated ones. By adding the [@GeneratorOptions](core/src/main/java/io/dekorate/annotation/GeneratorOptions.java).

##### Integration with Fabric8 Maven Plugin.

The fabric8-maven-plugin can be used to package applications for kubernetes and openshift. It also supports generating manifests.
A user might choose to build images using fmp, but customize them using `dekorate` annotations instead of xml.

An example could be to expose an additional port:

This can by done by configuring dekorate to read the fmp generated manifests from `META-INF/fabric8` which is where fmp stores them and save them back there once decoration is done.
```java
@GeneratorOptions(inputPath = "META-INF/fabric8", outputPath = "META-INF/fabric8")
@KubernetesApplication(port = @Port(name="srv", containerPort=8181)
public class Main {
   ... 
}
```
#### related examples
 - [spring boot with fmp on openshift example](examples/spring-boot-with-fmp-on-kubernetes-example)


## Want to get involved?

By all means please do! We love contributions! 
Docs, Bug fixes, New features ... everything is important!

Make sure you take a look at contributor [guidelines](contributor-guideliness.md).
Also, it can be useful to have a look at the dekorate [design](design.md).
