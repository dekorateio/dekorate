
![dekorate logo](assets/images/logo.png "Dekorate") 

![Build](https://github.com/dekorateio/dekorate/actions/workflows/build.yml/badge.svg)
![Integration Tests](https://github.com/dekorateio/dekorate/actions/workflows/integration-tests.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.dekorate/kubernetes-annotations.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.dekorate%22%20AND%20a:%22kubernetes-annotations%22)

Dekorate is a collection of Java compile-time generators and decorators for Kubernetes/OpenShift manifests.

It makes generating Kubernetes manifests as easy as adding a dependency to the classpath and customizing as simple as setting an annotation or application property.

Stop wasting time editing xml, json and yml and customize the kubernetes manifests as you configure your java application.

## Rebranding Notice

This project was originally called `ap4k` which stood for `Annotation Processors for Kubernetes`.
As the project now supports `decorating` of kubernetes manifests without the use of annotations, the name `ap4k` no longer describes the project in the best possible way. So, the project has been renamed to `dekorate`.

## Features

- Generates manifest via annotation processing
  - [Kubernetes](#kubernetes)
  - [OpenShift](#openshift)
  - [Knative](#knative)
  - [Tekton](#tekton)
  - [Prometheus](#prometheus)
  - [Jaeger](#jaeger)
  - [ServiceBinding CRD](#servicebinding-crd)
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
    - service instances
    - inject bindings into pods
- Build tool independent (works with maven, gradle, bazel and so on)
- [Rich framework integration](#framework-integration)
  - Port, Service and Probe auto configuration
    - Generic Java
    - Spring Boot
    - [Quarkus](#quarkus)
- [Configuration externalization for known frameworks](#configuration-externalization) (annotationless)
  - Spring Boot
- Integration with external generators
- [Rich set of examples](examples)
- [Explicit configuration of annotation processors](#explicit-configuration-of-annotation-processors)
- junit5 integration testing extension
  - [Kubernetes](#kubernetes-extension-for-junit5)
  - [OpenShift](#openshift-extension-for-junit5)
  - [Multi-Module testing](#testing-multi-module-projects)

### Experimental features

- Register hooks for triggering builds and deployment
  - Build hooks
    - [Docker build hook](#docker-build-hook)
    - Source to image build hook
    - Jib build hook

## Rationale

There are tons of tools out there for scaffolding / generating kubernetes
 manifests. Sooner or later these manifests will require customization.
Handcrafting is not an appealing option.
Using external tools, is often too generic.
Using build tool extensions and adding configuration via xml, groovy etc is a step forward, but still not optimal.

Annotation processing has quite a few advantages over external tools or build tool extensions:

- Configuration is validated by the compiler.
- Leverages tools like the IDE for writing type safe config (checking, completion etc).
- Works with all build tools.
- Can "react" to annotations provided by the framework.

## Hello World

This section provides examples on how to get started based on the framework you are using.

> NOTE: All examples in README using the version that corresponds to the target branch.
> On github master that is the latest 2.x release.

### Hello Spring Boot

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>2.7.0</version>
</dependency>
```

That's all! Next time you perform a build, using something like:

    mvn clean package
    
The generated manifests can be found under `target/classes/META-INF/dekorate`.

![asciicast](assets/images/dekorate-spring-hello-world.gif "Dekorate Spring Boot Hello World Asciicast") 

#### related examples
 - [spring boot on kubernetes example](examples/spring-boot-on-kubernetes-example)
 - [spring boot on openshift example](examples/spring-boot-on-openshift-example)

## Hello Quarkus

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-kubernetes</artifactId>
  <version>1.0.0.Final</version>
</dependency>
```

That's all! Next time you perform a build, using something like:

    mvn clean package
    
The generated manifests can be found under `target/kubernetes`.
Note: [Quarkus](https://quarkus.io) is using its own `dekorate` based Kubernetes extension (see more at  [Quarkus](#quarkus)).

![asciicast](assets/images/dekorate-quarkus-hello-world.gif "Dekorate Quarkus Hello World Asciicast") 

### Hello Thorntail

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>thorntail-spring-starter</artifactId>
  <version>2.7.0</version>
</dependency>
```

That's all! Next time you perform a build, using something like:

    mvn clean package
    
The generated manifests can be found under `target/classes/META-INF/dekorate`.


![asciicast](assets/images/dekorate-thorntail-hello-world.gif "Dekorate Thorntail Hello World Asciicast") 

#### related examples
 - [thorntail on kubernetes example](examples/thorntail-on-kubernetes-example)
 - [thorntail on openshift example](examples/thorntail-on-openshift-example)

### Hello Generic Java Application

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>2.7.0</version>
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


![asciicast](assets/images/dekorate-vertx-hello-world.gif "Dekorate Vert.X Hello World Asciicast") 

#### related examples
 - [vertx on kubernetes example](examples/vertx-on-kubernetes-example)
 - [vertx on openshift example](examples/vertx-on-openshift-example)

## Usage

To start using this project you just need to add one of the provided dependencies to your project.
For known frameworks like [spring boot](https://spring.io/projects/spring-boot), [quarkus](https://quarkus.io), or [thorntail](https://thorntail.io) that's enough.
For generic java projects, we also need to add an annotation that expresses our intent to enable `dekorate`.

This annotation can be either [@Dekorate](core/src/main/java/io/dekorate/annotation/Dekorate.java) or a more specialized one, which also gives us access to more specific configuration options.
Further configuration is feasible using:

- Java annotations
- Configuration properties (application.properties)
- Both 

A complete reference of the supported properties can be found in the [configuration options guide](assets/config.md).

### Kubernetes

[@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) is a more specialized form of [@Dekorate](core/src/main/java/io/dekorate/annotation/Dekorate.java).
It can be added to your project like:

```java
import io.dekorate.kubernetes.annotation.KubernetesApplication;

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
  <version>2.7.0</version>
</dependency>
```


#### Name and Version

So where did the generated `Deployment` gets its name, docker image etc from?

Everything can be customized via annotation parameters, application configuration and system properties.
On top of that, lightweight integration with build tools is provided in order to reduce duplication.

Note, that part-of, name and version are part of multiple annotations / configuration groups etc.

When a single application configuration is found and no explict image configuration value has been used for (group, name & version), values from the application configuration will be used.

For example:

```java
@KubernetesApplication(name="my-app")
@DockerBuild(registry="quay.io")
public class Main {
}
```

In the example above, docker is configured with no explicit value on `name`. In this case that name from `@KubernetesApplication(name="my-app")` will be used.

The same applies when property configuration is used:

```
io.dekorate.kubernetes.name=my-app
io.dekorate.docker.registry=quay.io
```


Note: Application configuration `part-of` corresponds to image configuration `group`. 

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

For all other build tools, the name and version need to be provided via `application.properties`:

    dekorate.kubernetes.name=my-app
    dekorate.kubernetes.version=1.1.0.Final

or the core annotations:

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
      app.kubernetes.io/name: "my-gradle-app"
      app.kubernetes.io/version: "1.0-SNAPSHOT"
  template:
    metadata:
      labels:
        app.kubernetes.io/name: "my-gradle-app"
        app.kubernetes.io/version: "1.0-SNAPSHOT"
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

To add extra ports to the container, you can add one or more `@Port` into your  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java):

```java
import io.dekorate.kubernetes.annotation.Port;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(ports = @Port(name = "web", containerPort = 8080))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

This will trigger the addition of a container port to the `Deployment` but also will trigger the generation of a `Service` resource.

Everything that can be defined using annotations, can also be defined using `application.properties`.
To add a port using `application.properties`:

    dekorate.kubernetes.ports[0].name=web
    dekorate.kubernetes.ports[0].container-port=8080
    
**NOTE:**  This doesn't need to be done explicitly, if the application framework is detected and support, ports can be extracted from there *(see below)*.

**IMPORTANT**: When mixing annotations and `application.properties` the latter will always take precedence overriding values that defined using annotations.
This allows users to define the configuration using annotations and externalize configuration to `application.properties`.

**REMINDER**: A complete reference on all the supported properties can be found in the [configuration options guide](assets/config.md).

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

To add environment variables using `application.properties`:

    dekorate.kubernetes.env-vars[0].name=key1
    dekorate.kubernetes.env-vars[0].value=value1

#### Adding environment variables from ConfigMap

To add an environment variable that points to a ConfigMap property, you need to specify the configmap using the `configmap` property in the @Env annotation.
The configmap key will be specified by the `value` property. So, in this case `value` has the meaning of `value from key`.

```java
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(envVars = @Env(name = "key1", configmap="my-config", value = "key1"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

To add an environment variable referencing a config map using `application.properties`:

    dekorate.
    .env-vars[0].name=key1
    dekorate.kubernetes.env-vars[0].value=key1
    dekorate.kubernetes.env-vars[0].config-map=my-config


#### Adding environment variables from Secrets

To add an environment variable that points to a Secret property, you need to specify the configmap using the `secret` property in the @Env annotation.
The secret key will be specified by the `value` property. So, in this case `value` has the meaning of `value from key`.

```java
import io.dekorate.kubernetes.annotation.Env;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(envVars = @Env(name = "key1", secret="my-secret", value = "key1"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

To add an environment variable referencing a secret using `application.properties`:

    dekorate.kubernetes.env-vars[0].name=key1
    dekorate.kubernetes.env-vars[0].value=key1
    dekorate.kubernetes.env-vars[0].secret=my-config

#### Working with volumes and mounts

To define volumes and mounts for your application, you can use something like:
```java
import io.dekorate.kubernetes.annotation.Mount;
import io.dekorate.kubernetes.annotation.PersistentVolumeClaimVolume;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(pvcVolumes = @PersistentVolumeClaimVolume(volumeName = "mysql-volume", claimName = "mysql-pvc"),
  mounts = @Mount(name = "mysql-volume", path = "/var/lib/mysql")
)
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```    

To define the same volume and mount via `application.properties`:

    dekorate.kubernetes.pvc-volumes[0].volume-name=mysql-volume
    dekorate.kubernetes.pvc-volumes[0].claim-name=mysql-pvc
    dekorate.kubernetes.mounts[0].name=mysql-volume
    dekorate.kubernetes.mounts[0].path=/var/lib/mysql
    
Currently, the supported annotations for specifying volumes are:

- @PersistentVolumeClaimVolume
- @SecretVolume
- @ConfigMapVolume
- @AwsElasticBlockStoreVolume
- @AzureDiskVolume
- @AzureFileVolume
   
#### Vcs Options
Most of the generated resources contain the kubernetes recommended annotations for specifying things like:

- vcs url
- commit id

These are extracted from the project `.git/config` file (Currently only git is supported).
Out of the box, the url of the `origin` remote will be used verbatim.

##### Specifying remote

In some cases users may prefer to use another remote.
This can be done with the use of `@VcsOptions` annotation:

```java
import io.dekorate.options.annotation.JvmOptions;
import io.dekorate.options.annotation.GarbageCollector;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication
@VcsOptions(remote="myfork")
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```
In the example above `myfork` will be used as the remote. So, generated resources will be annotated with the url of the `myfork` remote.

For users that prefer using `application.properties`:


    dekorate.vcs.remote=myfork


##### Converting vcs urls to https

The vcs related annotations are mostly used by tools. For public repositories its often simpler for tools, to access the repository anonymous access.
This is possible when using git over https, but not possible when using git over ssh. So, there are cases where users would rather develop using `git+ssh` 
but have 3d-party tools use `https` instead. To force dekorate covnert vcs urls to `https` one case use the `httpsPreferred` parameter of `@VcsOptions`.
Or using properties:

    dekoarate.vcs.https-preferred=true


#### Jvm Options
It's common to pass the JVM options in the manifests using the `JAVA_OPTS` or `JAVA_OPTIONS` environment variable of the application container.
This is something complex as it usually difficult to remember all options by heart and thus its error prone.
The worst part is that you usually don't realize the mistake until it's TOO
 late.

Dekorate provides a way to manage those options using the `@JvmOptions` annotation, which is included in the `options-annotations` module.

```java
import io.dekorate.options.annotation.JvmOptions;
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

or via `application.properties`:

    dekorate.jvm.server=true
    dekorate.jvm.xmx=1024
    dekorate.jvm.prefer-ipv4-stack=true
    dekorate.jvm.gc=GarbageCollector.SerialGC

This module can be added to the project using:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>option-annotations</artifactId>
  <version>2.7.0</version>
</dependency>
```

**Note**: The module is included in all starters.
    
### Container Resources

Kubernets allwos setting rules about container resources:

- Request CPU: The amount of CPU the container needs.
- Request Memory: The amount of memory the container needs.
- Limit CPU: The maximum amount of CPU the container will get.
- Limit Memory: The maximum amount of memory the container will get.

More information: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers

Dekorate supports these options for both the application container and / or any of the side car containers.

#### Application Container resources

##### Using annotations
There are parameters availbe for `@KubernetesApplication`, `@KnativeApplication` and `@OpenshiftApplication`.

Using the `@KubernetesApplication` one could set the resources like:

```java
import io.dekorate.kubernetes.annotation.ResourceRequirements;
import io.dekorate.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(requestResources=@ResourceRequirements(memory="64Mi", cpu="1m"), limitResources=@ResourceRequirements(memory="256Mi", cpu="5m"))
public class Main {
}
```

In the same spirit it workds for `@KnativeApplication` and `@OpenshiftApplication`.

##### Using properties

Users that prefer to configure dekorate using property configuration can use the following options:

```
dekorate.kubernetes.request-resources.cpu=1m
dekorate.kubernetes.request-resources.memory=64Mi
dekorate.kubernetes.limit-resources.cpu=5m
dekorate.kubernetes.limit-resources.memory=256Mi
```

In a similar manner works for openshift:

```
dekorate.openshift.request-resources.cpu=1m
dekorate.openshift.request-resources.memory=64Mi
dekorate.openshift.limit-resources.cpu=5m
dekorate.openshift.limit-resources.memory=256Mi
```


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

or via `application.properties`:

    dekorate.kubernetes.init-containers[0].image=foo/bar:latest
    dekorate.kubernetes.init-containers[0].command=foo
    

The [@Container](core/src/main/java/io/dekorate/kubernetes/annotation/Container.java) supports the following fields:

- Image
- Image Pull Policy
- Commands
- Arguments
- Environment Variables
- Mounts
- Probes

#### Sidecars

Similarly, to [init containers](#init-containers) support for sidecars is
 also provided using the `sidecars` property. For example:
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

or via `application.properties`:

    dekorate.kubernetes.sidecars[0].image=jaegertracing/jaeger-agent
    dekorate.kuberentes.args=--collector.host-port=jaeger-collector.jaeger-infra.svc:14267

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
  <version>2.7.0</version>
</dependency>
```
### OpenShift 

[@OpenshiftApplication](annotations/openshift-annotations/src/main/java/io/dekorate/openshift/annotation/OpenshiftApplication.java) works exactly like  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) , but will generate resources in a file name `openshift.yml` / `openshift.json` instead.
Also instead of creating a `Deployment` it will create a `DeploymentConfig`.

**NOTE:** A project can use both [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) and [@OpenshiftApplication](annotations/openshift-annotations/src/main/java/io/dekorate/openshift/annotation/OpenshiftApplication.java). If both the kubernetes and
OpenShift annotation processors are present both kubernetes and OpenShift
 resources will be generated. 

#### Adding the OpenShift annotation processor to the classpath

This module can be added to the project using:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-annotations</artifactId>
  <version>2.7.0</version>
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

The same can be expressed via `application.properties`:

    dekorate.openshift.name=doc-example
    
**IMPORTANT:** All examples of `application.properties` demonstrated in the [Kubernetes](#kubernetes) section can be applied here, by replacing the prefix `dekorate.kubernetes` with `dekorate.openshift`.

The generated `BuildConfig` will be a binary config. The actual build can be triggered from the command line with something like:

    oc start-build doc-example --from-dir=./target --follow

**NOTE:** In the example above we explicitly set a name for our application, and we referenced that name from the cli. 
If the name was implicitly created the user would have to figure the name out before triggering the build. This could be
done either by `oc get bc` or by knowing the conventions used to read names from build tool config (e.g. if maven then name the artifactId).

#### related examples

- [spring boot on openshift example](examples/spring-boot-on-openshift-example)
- [spring boot with groovy on openshift example](examples/spring-boot-with-groovy-on-openshift-example)
- [spring boot with gradle on openshift example](examples/spring-boot-with-gradle-on-openshift-example) 

### Tekton

Dekorate supports generating `tekton` pipelines.
Since Dekorate knows, how your project is build, packaged into containers and
deployed, converting that knowledge into a pipeline comes natural.

When the `tekton` module is added to the project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>tekton-annotations</artifactId>
  <version>2.7.0</version>
</dependency>
```

Two sets of resources will be generated, each representing a different configuration style the use user can choose from: 

- Pipeline based 
  - tekton-pipeline.yml
  - tekton-pipeline-run.yml
  - tekton-pipeline.json
  - tekton-pipeline-run.json
- Task based 
  - tekton-task.yml
  - tekton-task-run.yml
  - tekton-task.json
  - tekton-task-run.json

#### Pipeline

This set of resources contains:

- Pipeline
- PipelineResource (git, output image)
- PipelineRun
- Task (build, package and push, deploy)
- RBAC resources

These are the building blocks of a Tekton pipeline that grabs your project from
scm, builds and containerizes the project (in cluster) and finally deploys it.

#### Task 

This set of resources provides the some functionality as above, but everything
is collapsed under a single task (for usability reasons), In detail it contains:

- PipelineResource (git, output image)
- Task 
- TaskRun
- RBAC resources

#### Pipeline vs Task

If unsure which style to pickup, note that the `task` style has less
configuration requirements and thus easier to begin with. The `pipeline` style
is easier to slice and dice, once your are more comfortable with `tekton`.

Regardless of the choice, Dekorate provides a rich set of configuration options
to make using `tekton` as easy as it gets.

#### Tekton Configuration

##### Git Resource

The generated tasks and pipelines, assume the project is under version control and more specifically git.
So, in order to `run` the pipeline or the `task` a `PiepelineResource` of type `git` is required.
If the project is added to git, the resource will be generated for you. If for any reason the use of an external resource is 
preferred then it needs to be configured, like:

```
dekorate.tekton.external-git-pipeline-resource=<<the name of the resource goes here>>
```

##### Builder Image

Both the pipeline and the task based resources include steps that perform a
build of the project. Dekorate, tries to identify a suitable builder image for
the project. Selection is based on the build tool, jdk version, jdk flavor and
build tool version (in that order). At the moment only maven and gradle are supported.

You can customize the build task by specifying:

- custom builder image: `dekorate.tekton.builder-image`
- custom build command: `dekorate.tekton.builder-command`
- custom build arguments: `dekorate.tekton.builder-arguments`

##### Configuring a Workspace PVC

One of the main differences between the two styles of configuration, is that
Pipelines require a `PersistentVolumeClaim` in order to share the workspace
between Tasks. On the contrary when all steps are part of single bit fat Task
(which is baked by a Pod) and `EmptyDir` volume will suffice.

Out of the box, for the pipeline style resources a `PersistentVolumeClaim` named
after the application will be generated and used.

The generated pvc can be customized using the following properties:

  - dekorate.tekton.source-workspace-size (defaults to `1Gi`)
  - dekorate.tekton.source-workspace-storage-class (defaults to `standard`)
  
The option to provide an existing pvc (by name) instead of generating one is also
provided, using `dekorate.tekton.source-workspace-claim`.

##### Configuring the Docker registry for Tekton

The generated Pipeline / Task includes steps for building a container image and
pushing it to a registry.

The registry can be configured using `dekorate.docker.registry` as is done for
the rest of the resources.

For the push to succeed credentials for the registry are required.
The user is able to:

- Provide own Secret with registry credentials
- Provide username and password
- Upload local `.docker/config.json`

To provide an existing secret for the job (e.g. `my-secret`):

```
dekorate.tekton.image-builder-secert=my-secert
```

To provide username and password:

```
dekorate.tekton.registry-usernmae=myusername
dekorate.tekton.registry-password=mypassword
```

If none of the above is provided and a `.docker/config.json` exists, it can be
used if explicitly requested:

```
dekorate.tekton.use-local-docker-config-json=true
```



### Knative

Dekorate also supports generating manifests for `knative`. To make use of
 this feature you need to add:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>knative-annotations</artifactId>
  <version>2.7.0</version>
</dependency>
```

This module provides the 
[@KnativeApplication](annotations/knative-annotations/src/main/java/io/dekorate/knative/annotation/KnativeApplication.java) works exactly like  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) , but will generate resources in a file name `knative.yml` / `knative.json` instead.
Also instead of creating a `Deployment` it will create a knative serving `Service`.

#### Cluster local services

Knative `exposes` services out of the box. You can use the `@KnativeApplication(expose=false)` or the property `dekorate.knative.expose` set to false, in order to mark a service as cluster local.

#### Autoscaling
Dekorate provides access to both revision and global autoscaling configuration (see [Knative Autoscaling](https://knative.dev/docs/serving/configuring-autoscaling/).

Global autoscaling configuration is supported via configmaps (`KnativeServing` is not supported yet).

##### Class

To set the autoscaler class for the target revision:

```
dekorate.knative.revision-auto-scaling.autoscaler-class=hpa
```

The allowed values are:

- `hpa`: Horizontal Pod Autoscaler
- `kpa`: Knative Pod Autoscaler (default)

In the same spirit the global autoscaler class can be set using:

```
dekorate.knative.global-auto-scaling.autoscaler-class=hpa
```

##### Metric

To select the autoscaling metric:

```
dekorate.knative.revision-auto-scaling.metric=rps
```

The allowed values are:

- `concurrency`: Concurrency (default)
- `rps`: Requests per second
- `cpu`: CPU (requires `hpa` revision autoscaler class).

##### Target

Metric specifies the metric kind. To sepcify the target value the autoscaler should aim to maintain, the `target` can be used:

```
dekorate.knative.revision-auto-scaling.target=100
```

There is no option to set a generic global target. Instead specific keys per metric kind are provided. See below:

##### Requests per second

To set the requests per second:

```
dekorate.knative.global-auto-scaling.requests-per-second=100
```

##### Target utilization

To set the target utilization:

```
dekorate.knative.global-auto-scaling.target-utilization-percentage=100
```

###  Framework integration

Framework integration modules are provided that we are able to detect framework annotations and adapt to the framework (e.g. expose ports).

The frameworks supported so far: 

- Spring Boot
- Quarkus
- Thorntail

#### Spring Boot

With spring boot, we suggest you start with one of the provided starters:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>2.7.0</version>
</dependency>
```

Or if you are on [OpenShift](https://openshift.com):

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshfit-spring-starter</artifactId>
  <version>2.7.0</version>
</dependency>
```

#### Automatic configuration

For Spring Boot application, dekorate will automatically detect known annotation and will align generated manifests accordingly.

##### Exposing servies

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

#### Thorntail

With Thorntail, it is recommended to add a dependency on one of the provided starters:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>kubernetes-thorntail-starter</artifactId>
  <version>2.7.0</version>
  <scope>provided</scope>
</dependency>
```

Or, if you use [OpenShift](https://openshift.com):

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshfit-thorntail-starter</artifactId>
  <version>2.7.0</version>
  <scope>provided</scope>
</dependency>
```

Then, you can use the annotations described above, such `@KubernetesApplication`, `@OpenShiftApplication`, etc.

Note that the Thorntail annotation processor reads the `thorntail.http.port` configuration from the usual `project-defaults.yml`.
It doesn't read any other `project-*.yml` profiles.

## Experimental features

Apart from the core feature, which is resource generation, there are a couple of experimental features that do add to the developer experience.

These features have to do with things like building, deploying and testing.

### Building and Deploying?
Dekorate does not generate Docker files, neither it provides internal support
 for performing docker or s2i builds.
It does however allow the user to hook external tools (e.g. the `docker` or `oc`) to trigger container image builds after the end of compilation.

So, at the moment as an experimental feature the following hooks are provided:

- docker build hook (requires docker binary, triggered with `-Ddekorate.build=true`)
- docker push hook (requires docker binary, triggered with `-Ddekorate.push=true`)
- OpenShift s2i build hook (requires oc binary, triggered with `-Ddekorate.deploy=true`)
- [KiND](https://kind.sigs.k8s.io/) docker images loading hook (requires `kind`, triggered with `-Ddekorate.kind.autoload=true`)

#### Docker build hook
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
    
#### S2i build hook
This hook will just trigger an s2i binary build, that will pass the output folder as an input to the build

To enable the docker build hook you need:

- the `openshift-annotations` module (already included in all OpenShift starter modules)
- the `oc` binary configured to point the docker daemon of your kubernetes environment.

Finally, to trigger the hook, you need to pass `-Ddekorate.build=true`  as an argument to the build, for example:
```bash
mvn clean install -Ddekorate.build=true
```   
or if you are using gradle:
```bash
gradle build -Ddekorate.build=true  
```    

#### Jib build hook
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
  <version>2.7.0</version>
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

### Prometheus annotations

The [prometheus](https://prometheus.io/) annotation processor provides annotations for generating prometheus related resources.
In particular, it can generate [ServiceMonitor](annotations/prometheus-annotations/src/main/java/io/dekorate/prometheus/model/ServiceMonitor.java) which are used by the
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
Note: Some framework integration modules may further decorate the ServiceMonitor with framework specific configuration.
For example, the Spring Boot module will decorate the monitor with the Spring Boot specific path, which is `/actuator/prometheus`.

#### related examples
- [spring boot with prometheus on kubernetes example](examples/spring-boot-with-prometheus-on-kubernetes-example)

### Jaeger annotations

The [jaeger](https://www.jaegertracing.io) annotation processor provides annotations for injecting the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent) into the application pod.

Most of the work is done with the use of the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/dekorate/jaeger/annotation/EnableJaegerAgent.java) annotation.

#### Using the Jaeger Operator

When the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) is available, you set the `operatorEnabled` property to `true`.
The annotation processor will automatically set the required annotations to the generated deployment, so that the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) can inject the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent).

Here's an example:
```java
import io.dekorate.kubernetes.annotation.KubernentesApplication;
import io.dekorate.jaeger.annotation.EnableJaegerAgent;

@KubernetesApplication
@EnableJaegerAgent(operatorEnabled = true)
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
- [spring boot with jaeger on kubernetes example](examples/spring-boot-with-jaeger-on-kubernetes-example)

### ServiceBinding CRD 
[Service Binding Operator](https://github.com/redhat-developer/service-binding-operator) enables the application developers to bind the services that are backed by Kubernetes operators to an application that is deployed in kubernetes without having to perform manual configuration.
Dekorate supports generation of ServiceBinding CR.
The generation of ServiceBinding CR is triggered by annotating one of your classes with `@ServiceBinding` annotation and by adding the below dependency to the project and when the project gets compiled, the annotation will trigger the generation of ServiceBinding CR in both json and yml formats under the `target/classes/META-INF/dekorate`. The name of the ServiceBinding CR would be the name of the `applicationName + "-binding"`, for example if the application name is `sample-app`, the binding name would be `sample-app-binding`
```
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>servicebinding-annotations</artifactId>
</dependency>
```
Here is the simple example of using ServiceBinding annotations in SpringBoot application.
```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.dekorate.servicebinding.annotation.Service;
import io.dekorate.servicebinding.annotation.ServiceBinding;
import io.dekorate.servicebinding.annotation.BindingPath;
@ServiceBinding(
  services = {
    @Service(group = "postgresql.dev", name = "demo-database", kind = "Database", version = "v1alpha1", id = "postgresDB") })
@SpringBootApplication
public class Main {
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```
For someone who wants to configure the ServiceBinding CR using system properties, they can do it in the application.properties. The ServiceBinding CR can be customized either via annotation parameters or via system properties. The parameter values provided via annotations can be overrided by configuring the ServiceBinding CR in application.properties.
```
dekorate.servicebinding.services[0].name=demo-database
dekorate.servicebinding.services[0].group=postgresql.dev
dekorate.servicebinding.services[0].kind=Database
dekorate.servicebinding.services[0].id=postgresDB
```
Generated ServiceBinding CR would look something like this:
```
apiVersion: operators.coreos.com/v1beta1
kind: ServiceBinding
metadata:
  name: servicebinding-binding-example
spec:
  application:
    group: apps
    resource: Deployment
    name: servicebinding-example
    version: v1
  services:
  - group: postgresql.dev
    kind: Database
    name: demo-database
    version: v1alpha1
    id: postgresDB
  detectBindingResources: false
  bindAsFiles: false
```
If the application's `bindingPath` needs to configured, `@BindingPath` annotation can be used directly under `@ServicingBinding` annotation. For example:
```
@ServiceBinding(
  bindingPath = @BindingPath(containerPath="spec.template.spec.containers")
  services = {
    @Service(group = "postgresql.dev", name = "demo-database", kind = "Database", version = "v1alpha1", id = "postgresDB") }, envVarPrefix = "postgresql")
@SpringBootApplication
```
**Note** : `ServiceBinding` annotations are already usuable though still highly experimental. The Service Binding operator is still in flux and may change in the near future.


#### External generator integration

No matter how good a generator/scaffolding tool is, its often desirable to handcraft part of it.
Other times it might be desirable to combine different tools together (e.g. to generate the manifests using fmp but customize them via dekorate annotations)

No matter what the reason is, dekorate supports working on existing resources and decorating them based on the provided annotation configuration.
This is as simple as letting dekorate know where to read the existing manifests and where to store the generated ones. By adding the [@GeneratorOptions](annotations/option-annotations/src/main/java/io/dekorate/option/annotation/GeneratorOptions.java).

##### Integration with Fabric8 Maven Plugin.

The fabric8-maven-plugin can be used to package applications for kubernetes and OpenShift. It also supports generating manifests.
A user might choose to build images using fmp, but customize them using `dekorate` annotations instead of xml.

An example could be to expose an additional port:

This can be done by configuring dekorate to read the fmp generated manifests
 from `META-INF/fabric8` which is where fmp stores them and save them back
  there once decoration is finished.
```java
@GeneratorOptions(inputPath = "META-INF/fabric8", outputPath = "META-INF/fabric8")
@KubernetesApplication(port = @Port(name="srv", containerPort=8181)
public class Main {
   ... 
}
```
#### related examples
 - [spring boot with fmp on openshift example](examples/spring-boot-with-fmp-on-kubernetes-example)


#### Debugging and Logging

To control how verbose the dekorate output is going to be you can set the log level level threshold, using the `io.dekorate.log.level` system property-drawer.

Allowed values are:

- OFF
- ERROR
- WARN
- INFO (default)
- DEBUG

#### Explicit configuration of annotation processors

By default, Dekorate doesn't require any specific configuration of its annotation processors. 
However, it is possible to manually define the annotation processors if
 required.

In the maven pom.xml configure the annotation processor path in the maven compiler plugin settings. 

The example below configures the Mapstruct, Lombok and Dekorate annotation processors

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven-compiler-plugin.version}</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>io.dekorate</groupId>
                            <artifactId>kubernetes-annotations</artifactId>
                            <version>2.7.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin> 
```

### Using the bom

Dekorate provides a bom, that offers dependency management for dekorate artifacts.

The bom can be imported like:

```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
               <groupId>io.dekorate</groupId>
               <artifactId>dekorate-bom</artifactId>
               <version>2.7.0</version>
               <type>pom</type>
               <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

#### Using with downstream BOMs

In case, that dekorate bom is imported by a downstream project (e.g. snowdrop) and its required to override the bom version, all you need to do is to import the dekorate bom with the version of your choice first.

## Versions and Branches

The current version of dekorate is `<version>2.0.0</version>`.

### What's changed in 2.x

Most of the changes that happend inside 2.x are internal and are related to the maintainance of the project.

#### New features

- Configurable logging threshold
- Git options
- Inferring image configuration from application config
- JaxRS support (without requiring Thorntail)
- Integration testing framework improvements (detailed diagnostics on error)
- Updated to kubernetes-client and model v5.1.1

#### Annotation naming

- EnableDockerBuild -> DockerBuild
- EnableS2iBuild -> S2iBuild
- EnableJibBuild -> JibBuild

#### Dropped modules

The following features were dropped:

- service catalog
- halkyon 
- application crd
- crd generator (functionality moved to the fabric8 kubernetes-client).
- dependencies uberjar

#### Dropped `dependencies` shadowed uber jar

Earlier version of dekorate used a shadowed uberjar containing all dependencies.
As of `2.0.0` the `dependencies` uberjar is no more.
Downstream projects using dekorate as a library will need to switch from `io.dekorate.deps.xxx` to the original packages.

#### Component naming

Earlier version of dekorate used names for its core components that we too generic.
So, in 2.0.0 the name changed so that they are more descriptive.
Naming changes:

- Generator -> ConfigGenerator
- Hanlder -> ManifestGenerator

### Branches

All dekorate development takes place on the `master` branch. From that branch `current` releases are created.
Bug fixes for older releases are done through their correspnding branch.

- master (active development, pull requests should point here)
- 1.0.x
- 0.15.x 

### Pull request guidelines

All pull requests should target the `master` branch and from there things are backported to where it makes sense.

## Frequently asked questions

### How do I tell dekorate to use a custom image name?

By default the image name used is `${group}/${name}:${version}` as extracted by the project / environment or explicitly configured by the user.
If you don't want to tinker those properties then you can:

#### Using annotations

Add `@DockerBuild(image="foo/bar:baz")` to the your main or whatever class you use to configure dekorate. If instead of docker you are using jib or s2i you can use `@JibBuild(image="foo/bar:baz")` or `@S2iBuild(image="foo/bar:baz")` respectively.

#### Using annotations

Add the following to your application.properties

```
dekorate.docker.image=foo/bar:baz
```

#### Using annotations

Add the following to your application.yaml

```
dekorate:
  docker:
    image: foo/bar:baz
```

#### related examples
 - [kubernetes with custom image name example](examples/kubernetes-with-custom-image-name-example)


## Want to get involved?

By all means please do! We love contributions! 
Docs, Bug fixes, New features ... everything is important!

Make sure you take a look at contributor [guidelines](assets/contributor-guidelines.md).
Also, it can be useful to have a look at the dekorate [design](assets/design.md).
