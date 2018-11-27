# Annotation processors for Kubernetes

[![CircleCI](https://circleci.com/gh/ap4k/ap4k.svg?style=svg)](https://circleci.com/gh/ap4k/ap4k)

Annotation processor for Kubernetes is a collection of Java annotation processors and tools for generating Kubernetes/Openshift resources at compile time.

## Features

- Kubernetes annotations
  - annotations
  - labels
  - environment variables
  - mounts
  - ports and services
- Openshift annotations 
  - image streams
  - build configurations
- Service Catalog annotations
  - service instances
  - inject bindings into pods
- Istio annotations
  - proxy injection
- Component operator annotations 
- No build tool coupling
- Rich framework integration
  - Spring Boot
  - Thorntail
  - Micronaut

### Experimental features

- Build hooks
  - Docker build hook
  - Source to image build hook


## Rationale

The are tons of tools out there for scaffolding / generating kubernetes resources. Sooner or later these resources will require customization and what better way to that than using java annotations?

Annotation processing has quite a few advantages over external tools or build tool extensions:

- Configuration is validated by the compiler.
- Leverages tools like the IDE for writing type safe config (checking, completion etc).
- Works with all build tools.
- Can "react" to annotations provided by the framework.
- Annotation processing is performed in rounds which makes it technically easier to write extensions and customizations.

#### What about building and deploying?
Triggering the build of a docker image using a Dockerfile or a S2i image is outside of the scope of this project but nevertheless,
it is proposed as an experimental option(using the hooks below) till we have figured out how such deployment/integration
will take place with different tools such as: 

- fabric8 maven plugin,
- odo,
- launcher

... or an operator able to process the resources generated.

So, at the moment as an experimental support the following hooks are provided:

- docker build hook (requires docker binary, triggered with `-Dap4k.build=true`)
- openshift s2i build hook (requires oc binary, triggered with `-Dap4k.deploy=true`)

## Usage

To start using this project you just need to add one of the provided annotations to your project.

### Kubernetes annotations

This module provides `@KubernetesApplication` which can be added to your project like:

    import io.ap4k.annotaion.KubernetesApplication;
    
    @KubernetesApplication
    public class Main {

        public static void main(String[] args) {
          //Your application code goes here.
        }
    }
    
When the project gets compiled, the annotation will trigger the generation of a `Deployment` in both json and yml that
will end up under 'target/classes/META-INF/apk'. 

The annotation comes with a lot of parameters, which can be used in order to customize the `Deployment` and/or trigger
the generations of addition resources, like `Service` and `Ingress`.

#### Name and Version

So where did the generated `Deployment` gets its name, docker image etc from?

Everything can be customized via annotation parameters and system properties.
On top of that `lightweight` integration with build tools is provided in order to reduce duplication.

##### Lightweight build tool integration

Lightweight integration with build tools, refers to reading information from the build tool config without bringing in the build tool itself into the classpath.
For example in the case of maven it refers to parsing the pom.xml with DOM in order to fetch the artifactId and version.

#### Adding extra ports and exposing them as services

To add extra ports to the container, you can add one or more `@Port` into your `@KubernetesApplication`:

    import io.ap4k.kubernetes.annotation.Env;
    import io.ap4k.kubernetes.annotation.KubernetesApplication;

    @KubernetesApplication(ports = @Port(name = "web", containerPort = 8080))
    public class Main {

      public static void main(String[] args) {
        //Your code goes here
      }
    }

This will trigger the addition of a container port to the `Deployment` but also will trigger the generation of a `Service` resource.

**Note:**  This doesn't need to be done explicitly, if the application framework is detected and support, ports can be extracted from there *(see below)*.

#### Adding container environment variables
To add extra environment variables to the container, you can add one or more `@EnvVar` into your `@KubernetesApplication`:

    import io.ap4k.kubernetes.annotation.Env;
    import io.ap4k.kubernetes.annotation.KubernetesApplication;

    @KubernetesApplication(envVars = @Env(name = "key1", value = "var1"))
    public class Main {

      public static void main(String[] args) {
        //Your code goes here
      }
    }
    
Additional options are provided for adding environment variables from fields, config maps and secrets.    

#### Working with volumes and mounts
To define volumes and mounts for your application, you can use something like:

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
    
    
Currently the supported annotations for specifying volumes are:

- @PersistentVolumeClaimVolume
- @SecretVolume
- @ConfigMapVolume
- @AwsElasticBlockStoreVolume
- @AzureDiskVolume
- @AzureFileVolume
   
#### Adding the kubernetes annotation processor to the classpath

This module can be added to the project using:

    <dependency>
     <groupId>io.ap4k</groupId>
     <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

### Openshift annotations

This module provides two new annotations: 

- @OpenshiftApplication
- @SourceToImage

`@OpenshiftApplication` works exactly like `@KubernetesApplication`, but will generate resources in a file name `openshift.yml` / `openshift.json` instead.
Also instead of creating a `Deployment` it will create a `DeploymentConfig`.

**NOTE:** A project can use both `@KubernetesApplication` and `@OpenshiftApplication`. If both the kubernetes and
openshift annotation processors are present both kubernetes and openshift resources will be generated. If only the openshift 
annotation processor is available (and @KubernetesApplication is transitively added) then just the opneshift resources will be 
generated.

#### Adding the kubernetes annotation processor to the classpath

This module can be added to the project using:

    <dependency>
     <groupId>io.ap4k</groupId>
     <artifactId>openshift-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
#### Integrating with S2i
To configure s2i for this project one can add the `@SourceToImage` annotation to the project.
This annotation will configure:

- ImageStream
  - builder 
  - target
- BuildConfig 

Here's an example:

    import io.ap4k.openshift.annotation.OpenshiftApplication;
    import io.ap4k.openshift.annotation.EnableS2iBuild;

    @OpenshiftApplication(name = "doc-example")
    @SourceToImage
    public class Main {

        public static void main(String[] args) {
          //Your code goes here
        }
    }
    
The generated `BuildConfig` will be a binary config. The actual build can be triggered from the command line with something like:

    oc start-build doc-example --from-dir=./target --follow

**NOTE:** In the example above we explicitly set a name for our application, and we refernced that name from the cli. 
If the name was implicitly created the user would have to figure the name out before triggering the build. This could be
done either by `oc get bc` or by knowing the conventions used to read names from build tool config (e.g. if maven then name the artifactId).

### Service Catalog annotations
The [service catalog](https://svc-cat.io) annotation processor is can be used in order to create [service catalog](https://svc-cat.io) resources for:

- creating service instances
- binding to services
- injecting binding info into the container 

Here's an example:

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

The `@ServiceCatalogInstance` annotation will trigger the generation of a `ServiceInstance` and a `ServiceBinding`resource.
It will also decorate any `Pod`, `Deployment`, `DeploymentConfig` and so on with additional environment variables containing the binding information.

#### Adding the service catalog annotation processor to the classpath

This module can be added to the project using:

    <dependency>
     <groupId>io.ap4k</groupId>
     <artifactId>servicecatalog-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
### Istio annotations

The [istio](https://istio.io)  annotation processor can be used to automatically inject the istio sidecar to the generated resources. 
For example:

    import io.ap4k.kubernetes.annotation.KubernetesApplication;
    import io.ap4k.istio.annotation.Istio;
   
    @Istio
    @KubernetesApplication
    public class Main {
         public static void main(String[] args) {
           //Your code goes here
         }
    }

#### Adding the istio annotation processor to the classpath

This module can be added to the project using:

    <dependency>
     <groupId>io.ap4k</groupId>
     <artifactId>istio-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
### Component annotations
The component [CRD](https://kubernetes.io/docs/tasks/access-kubernetes-api/custom-resources/custom-resource-definitions/) aims on abstracting kubernetes/OpenShift resources and simplify the config, design of an application.
See the following [project](https://github.com/snowdrop/component-operator/blob/master/pkg/apis/component/v1alpha1/component_types.go) to get more buildInfo about how the structure, syntax of a Component (runtime, service, link) is defined.
To play with a Components CRD and its [operator](https://coreos.com/operators/) running on the cloud platform and able to generate the kubernetes resources or manage them, then look to this [project](https://github.com/snowdrop/component-operator-demo).
This module provides limited/early support of the component operator.

By adding the `@CompositeApplication` annotation to the application, the generation of `target/classes/META-INF/apk/component.yml' is triggered.

The content of the component descriptor will be determined by the existing config provided by annotations like:

- @KubernetesApplication
- @ServiceCatalog
- and more...

For example, the following code:

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
    
Will trigger the creation of the following component:

     apiVersion: "v1beta1"
     kind: "Component"
     metadata:
       name: ""
     spec:
       deploymentMode: "innerloop"
    service:
    - name: "mysql-instance"
      class: "apb-mysql"
      plan: "default"
      secretName: "mysql-secret"

This module can be added to the project using:

    <dependency>
     <groupId>io.ap4k</groupId>
     <artifactId>component-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
###  Framework integration

Framework integration modules are provided that we are able to detect framework annotations and adapt to the framework (e.g. expose ports).

The frameworks supported so far: 

- Spring Boot
- Thorntail (or any framework using jaxrs, jaxws annotations)
- Micronaut

## Experimental features

### Docker build hook

A shutddown hook that triggers a docker build, given that the docker binary is present and is configured to point to a working docker daemon.
