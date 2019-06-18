# Annotation processors for Kubernetes

[![CircleCI](https://circleci.com/gh/ap4k/ap4k.svg?style=svg)](https://circleci.com/gh/ap4k/ap4k) [![Maven Central](https://img.shields.io/maven-central/v/io.ap4k/kubernetes-annotations.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.ap4k%22%20AND%20a:%22kubernetes-annotations%22)

Ap4k is a collection of Java annotations and processors for generating Kubernetes/OpenShift manifests at compile time.

It makes generating Kubernetes manifests as easy as adding:  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/ap4k/kubernetes/annotation/KubernetesApplication.java) on your main class (or any other class).

Stop wasting time editing xml, json and yml and customize the kubernetes manifests using annotations.

## Features

- Generates manifest via annotation processing
  - [Kubernetes](#kubernetes-annotations)
  - [OpenShift](#openshift-annotations)
  - [Prometheus](#prometheus-annotations)
  - [Jaeger](#jaeger-annotations)
  - [Service Catalog](#service-catalog-annotations)
  - [Component CRD](#component-annotations)
  - [Application CRD](#application-annotations)
  - Istio
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
  - Istio 
    - proxy injection
  - Component CRD
- Build tool independent (works with maven, gradle, bazel and so on)
- [Rich framework integration](#framework-integration)
  - Port, Service and Probe auto configuration
    - Spring Boot
    - Thorntail
    - Micronaut
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

[@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/ap4k/kubernetes/annotation/KubernetesApplication.java) can be added to your project like:

```java
import io.ap4k.kubernetes.annotaion.KubernetesApplication;

@KubernetesApplication
public class Main {

    public static void main(String[] args) {
      //Your application code goes here.
    }
}
```

When the project gets compiled, the annotation will trigger the generation of a `Deployment` in both json and yml that
will end up under 'target/classes/META-INF/apk'. 

The annotation comes with a lot of parameters, which can be used in order to customize the `Deployment` and/or trigger
the generations of addition resources, like `Service` and `Ingress`.

#### Adding the kubernetes annotation processor to the classpath

This module can be added to the project using:

```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

#### related examples
 - [kubernetes example](examples/kubernetes-example)


#### Name and Version

So where did the generated `Deployment` gets its name, docker images etc from?

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

To add extra ports to the container, you can add one or more `@Port` into your  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/ap4k/kubernetes/annotation/KubernetesApplication.java) :
```java
import io.ap4k.kubernetes.annotation.Env;
import io.ap4k.kubernetes.annotation.KubernetesApplication;

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
To add extra environment variables to the container, you can add one or more `@EnvVar` into your  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/ap4k/kubernetes/annotation/KubernetesApplication.java) :
```java
import io.ap4k.kubernetes.annotation.Env;
import io.ap4k.kubernetes.annotation.KubernetesApplication;

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
import io.ap4k.kubernetes.annotation.Port;
import io.ap4k.kubernetes.annotation.Mount;
import io.ap4k.kubernetes.annotation.PersistentVolumeClaimVolume;
import io.ap4k.kubernetes.annotation.KubernetesApplication;

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

Ap4k provides a way to manage those options using the `@JvmOption` annotation, which is included in the `options-annotations`.

```java
import io.ap4k.options.annotation.JvmOptions
import io.ap4k.options.annotation.GarbageCollector;
import io.ap4k.kubernetes.annotation.KubernetesApplication;

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
  <groupId>io.ap4k</groupId>
  <artifactId>option-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

Note: The module is included in all starters.    
    
#### Init Containers

If for any reason the application requires the use of init containers, they can be easily defined using the `initContainer`
property, as demonstrated below.
```java
import io.ap4k.kubernetes.annotation.Container;
import io.ap4k.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(initContainers = @Container(image="foo/bar:latest", command="foo"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

The [@Container](core/src/main/java/io/ap4k/kubernetes/annotation/Container.java) supports the following fields:

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
import io.ap4k.kubernetes.annotation.Container;
import io.ap4k.kubernetes.annotation.KubernetesApplication;

@KubernetesApplication(sidecars = @Container(image="jaegertracing/jaeger-agent",
                                             args="--collector.host-port=jaeger-collector.jaeger-infra.svc:14267"))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```

As in the case of [init containers](#init-containers) the [@Container](core/src/main/java/io/ap4k/kubernetes/annotation/Container.java) supports the following fields:

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
  <groupId>io.ap4k</groupId>
  <artifactId>kubernetes-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```
### OpenShift annotations

This module provides two new annotations: 

- @OpenshiftApplication

[@OpenshiftApplication](annotations/openshift-annotations/src/main/java/io/ap4k/openshift/annotation/OpenshiftApplication.java) works exactly like  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/ap4k/kubernetes/annotation/KubernetesApplication.java) , but will generate resources in a file name `openshift.yml` / `openshift.json` instead.
Also instead of creating a `Deployment` it will create a `DeploymentConfig`.

**NOTE:** A project can use both [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/ap4k/kubernetes/annotation/KubernetesApplication.java) and [@OpenshiftApplication](annotations/openshift-annotations/src/main/java/io/ap4k/openshift/annotation/OpenshiftApplication.java). If both the kubernetes and
openshift annotation processors are present both kubernetes and openshift resources will be generated. 

#### Adding the openshift annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.ap4k</groupId>
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
import io.ap4k.openshift.annotation.OpenshiftApplication;

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
In particular it can generate [ServiceMonitor](annotations/prometheus-annotations/src/main/java/io/ap4k/prometheus/model/ServiceMonitor.java) which are used by the
[Prometheus Operator](https://github.com/coreos/prometheus-operator) in order to configure [prometheus](https://prometheus.io/) to collect metrics from the target application.

This is done with the use of [@EnableServiceMonitor](annotations/prometheus-annotations/src/main/java/io/ap4k/prometheus/annotation/EnableServiceMonitor.java) annotation.

Here's an example:
```java
import io.ap4k.kubernetes.annotation.KubernentesApplication;
import io.ap4k.prometheus.annotation.EnableServiceMonitor;

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

Most of the work is done with the use of the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/ap4k/jaeger/annotation/EnableJaegerAgent.java) annotation.

#### Using the Jaeger Operator

When the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) is available, you set the `operatorEnabled` property to `true`.
The annotation processor will automicatlly set the required annotations to the generated deployment, so that the [jaeger operator](https://github.com/jaegertracing/jaeger-operator) can inject the [jaeger-agent](https://www.jaegertracing.io/docs/1.10/deployment/#agent).

Here's an example:
```java
import io.ap4k.kubernetes.annotation.KubernentesApplication;
import io.ap4k.jaeger.annotation.EnableJaegerAgent;

@KubernetesApplication
@EnableJaegerAgent(operatorEnabled="true")
public class Main {
    public static void main(String[] args) {
      //Your code goes here
    }
}
```    
##### Manually injection the agent sidecar

For the cases, where the operator is not present, you can use the [@EnableJaegerAgent](annotations/jaeger-annotations/src/main/java/io/ap4k/jaeger/annotation/EnableJaegerAgent.java) to manually configure the sidecar.

```java
import io.ap4k.kubernetes.annotation.KubernentesApplication;
import io.ap4k.jaeger.annotation.EnableJaegerAgent;

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
The [services catalog](https://svc-cat.io) annotation processor is can be used in order to create [services catalog](https://svc-cat.io) resources for:

- creating services instances
- binding to services
- injecting binding info into the container 

Here's an example:
```java
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.servicecatalog.annotation.ServiceCatalogInstance;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;

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

#### Adding the services catalog annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>servicecatalog-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

#### related examples
 - [service catalog example](examples/service-catalog-example)  
 
### Istio annotations

The [istio](https://istio.io)  annotation processor can be used to automatically inject the istio sidecar to the generated resources. 
For example:
```java
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.istio.annotation.Istio;

@Istio
@KubernetesApplication
public class Main {
     public static void main(String[] args) {
       //Your code goes here
     }
}
```
#### Adding the istio annotation processor to the classpath

This module can be added to the project using:
```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>istio-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```

### Component annotations
The component [CRD](https://kubernetes.io/docs/tasks/access-kubernetes-api/custom-resources/custom-resource-definitions/) aims on abstracting kubernetes/OpenShift resources and simplify the config, design of an application.
See the following [project](https://github.com/snowdrop/component-operator/blob/master/pkg/apis/component/v1alpha1/component_types.go) to get more buildInfo about how the structure, syntax of a Component (runtime, services, links) is defined.
To play with a Components CRD and its [operator](https://coreos.com/operators/) running on the cloud platform and able to generate the kubernetes resources or manage them, then look to this [project](https://github.com/snowdrop/component-operator-demo).
This module provides limited/early support of the component operator.

By adding the `@CompositeApplication` annotation to the application, the generation of `target/classes/META-INF/apk/component.yml' is triggered.

The content of the component descriptor will be determined by the existing config provided by annotations like:

- @KubernetesApplication
- @ServiceCatalog
- and more...

For example, the following code:

```java
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.component.annotation.CompositeApplication;
import io.ap4k.servicecatalog.annotation.ServiceCatalog;
import io.ap4k.servicecatalog.annotation.ServiceCatalogInstance;

@KubernetesApplication
@ServiceCatalog(instances = @ServiceCatalogInstance(name = "mysql-instance", serviceClass = "apb-mysql", servicePlan = "default", secretName="mysql-secret"))
@CompositeApplication
public class Main {

     public static void main(String[] args) {
         //Your code goes here 
     }
}
```

Will trigger the creation of the following component:

```yaml
 apiVersion: "v1beta1"
 kind: "Component"
 metadata:
   name: ""
 spec:
   deploymentMode: "innerloop"
services:
- name: "mysql-instance"
  class: "apb-mysql"
  plan: "default"
  secretName: "mysql-secret"
```
This module can be added to the project using:
```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>component-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```  
### Application Annotations

The [@EnableApplicationResource](annotations/application-annotations/src/main/java/io/ap4k/application/annotation/EnableApplicationResource.java) enables the generation of the `Application` custom resource, that is defined as part of https://github.com/kubernetes-sigs/application.

To use this annotation, one needs:
```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>application-annotations</artifactId>
  <version>${project.version}</version>
</dependency>
```
And then its just a matter of specifying:
```java
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.application.annotation.EnableApplicationResource;

@KubernetesApplication
@EnableApplicationResource(icons=@Icon(src="url/to/icon"), owners=@Contact(name="John Doe", email="john.doe@somemail.com"))
public class Main {

     public static void main(String[] args) {
         //Your code goes here 
     }
}
```
Along we the resources that ap4k usually generates, there will be also an `Application` custom resource.

    
###  Framework integration

Framework integration modules are provided that we are able to detect framework annotations and adapt to the framework (e.g. expose ports).

The frameworks supported so far: 

- Spring Boot
- Thorntail (or any framework using jaxrs, jaxws annotations)
- Micronaut

#### Spring Boot

With spring boot its suggested to start with one of the provided starters:

```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>kubernetes-spring-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

Or if you are on [openshift](https://openshift.com):

```xml
<dependency>
  <groupId>io.ap4k</groupId>
  <artifactId>openshfit-spring-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

##### Annotation less
For spring boot application all you need to do, is adding one of the starters to the classpath. No need to specify an additonal annotation.
This provides the fastest way to get started using [ap4k](https://github.com/ap4k/ap4k) with [spring boot](https://spring.io/projects/spring-boot).

Note: Still, if you need to customize the generted manifests, you still have to use annotations.

In future releases, it should be possible to fully customize the manifests just by using `application.properties`.

## Experimental features

Apart from the core feature, which is resource generation, there are a couple of experimental features that do add to the developer experience.

These features have to do with things like building, deploying and testing.

### Building and Deploying?
Ap4k does not generate Dockerfiles, neither it provides internal support for performing docker or s2i builds.
It does however allow the user to hook external tools (e.g. the `docker` or `oc`) to trigger container image builds after the end of compilation.

So, at the moment as an experimental feature the following hooks are provided:

- docker build hook (requires docker binary, triggered with `-Dap4k.build=true`)
- docker push hook (requires docker binary, triggered with `-Dap4k.push=true`)
- openshift s2i build hook (requires oc binary, triggered with `-Dap4k.deploy=true`)

#### Docker build hook
This hook will just trigger a docker build, using an existing Dockerfile at the root of the project.
It will not generate or customize the docker build in anyway.

To enable the docker build hook you need:

- a `Dockerfile` in the project/module root
- the `docker` binary configured to point the docker daemon of your kubernetes environment.

To trigger the hook, you need to pass `-Dap4k.build=true`  as an argument to the build, for example:
```bash
mvn clean install -Dap4k.build=true
```
or if you are using gradle:
```bash
gradle build -Dap4k.build=true   
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
mvn clean install -Dap4k.docker.registry=quay.io -Dap4k.push=true    
```

Note: Ap4k will **NOT** push images on its own. It will delegate to the `docker` binary. So the user needs to make sure
beforehand that is logged in and has taken all necessary actions for a `docker push` to work.
    
#### S2i build hook
This hook will just trigger an s2i binary build, that will pass the output folder as an input to the build

To enable the docker build hook you need:

- the `openshift-annotations` module (already included in all openshift starter modules)
- the `oc` binary configured to point the docker daemon of your kubernetes environment.

Finally, to trigger the hook, you need to pass `-Dap4k.build=true`  as an argument to the build, for example:
```bash
mvn clean install -Dap4k.build=true
```   
or if you are using gradle:
```bash
gradle build -Dap4k.build=true  
```    
### Junit5 extensions 

Ap4k provides two junit5 extensions for:

- Kubernetes
- Openshift

These extensions are `ap4k` aware and can read generated resources and configuration, in order to manage `end to end` tests
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
  <groupId>io.ap4k</groupId>
  <artifactId>kubernetes-junit</artifactId>
  <version>${project.version}</version>
</dependency>
```    
This dependency gives access to [@KubernetesIntegrationTest](testing/kubernetes-junit/src/main/java/io/ap4k/testing/annotation/KubernetesIntegrationTest.java) which is what enables the extension for your tests.

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

To inject one of this you need a field in the code annotated with [@Inject](testing/core-junit/src/main/java/io/ap4k/testing/annotation/Inject.java).

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

Similarly to using the [kubernetes junit extension](#kubernetes-extension-for-junit5) you can use the extension for OpenShift, by adding  [@OpenshiftIntegrationTest](testing/openshift-junit/src/main/java/io/ap4k/testing/annotation/OpenshiftIntegrationTest.java).
To use that you need to add:
```xml
<dependency>
  <groupId>io.ap4k</groupId>
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

Ap4k is graudally adding support for configuration externalization for the supported frameworks:

- spring boot

For these frameworks, the use of annotations is optional, as everything may be configured via configuration files.
Each annotation may be expressed using properties or yaml using the following steps.

- Each annotation property is expressed using a key/value pair.
- All keys start with the `ap4k.<annotation kind>.` prefix, where `annotation kind` is the annotation class name in lowercase, stripped of the `Application` suffix.
- The remaining part of key is the annotation property name.
- For nesting properties the key is also nested following the previous rule.

Examples:

The following annotation configuration:

    @KubernetesApplication(labels=@Label(key="foo", value="bar"))
    public class Main {
    }
    
Can be expressed using properties:

    ap4k.kubernetes.labels[0].key=foo
    ap4k.kubernetes.labels[0].value=bar
    
or using yaml:

    ap4k:
      kubernetes:
        labels:
          - key: foo
            value: bar
   
   
In the examples above, `ap4k` is the prefix that we use to `namespace` the ap4k configuration. `kubernetes` defines the annotation kind (its `@KubernetesApplication` in lower case and stripped of the `Application` suffix).
`labels`, `key` and value are the property names and since the `Label` is nested under `@KubernetesApplication` so are the properties.

The exact same example for openshift (where `@OpenshiftApplication` is used instead) would be:

   @OpenshiftApplication(labels=@Label(key="foo", value="bar"))
    public class Main {
    }
    
Can be expressed using properties:

    ap4k.openshift.labels[0].key=foo
    ap4k.openshift.labels[0].value=bar
    
or using yaml:

    ap4k:
      openshift:
        labels:
          - key: foo
            value: bar
   
##### Spring Boot

For spring boot, ap4k will look for configuration under:

- application.properties
- application.yml
- application.yaml

Also it will look for the same files under the kubernetes profile:

- application-kubernetes.properties
- application-kubernetes.yml
- application-kubernetes.yaml


#### External generator integration

No matter how good a generator/scaffolding tool is, its often desirable to handcraft part of it.
Other times it might be desirable to combine different tools together (e.g. to generate the manifests using fmp but customize them via ap4k annotations)

No matter what the reason is, ap4k supports working on existing resources and decorating them based on the provided annotation configuration.
This is as simple as letting ap4k know where to read the existing manifests and where to store the generated ones. By adding the [@GeneratorOptions](core/src/main/java/io/ap4k/annotation/GeneratorOptions.java).

##### Integration with Fabric8 Maven Plugin.

The fabric8-maven-plugin can be used to package applications for kubernetes and openshift. It also supports generating manifests.
A user might choose to build images using fmp, but customize them using `ap4k` annotations instead of xml.

An example could be to expose an additional port:

This can by done by configuring ap4k to read the fmp generated manifests from `META-INF/fabric8` which is where fmp stores them and save them back there once decoration is done.
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
Also, it can be useful to have a look at the ap4k [design](design.md).

