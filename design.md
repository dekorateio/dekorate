# AP4K Design

## Overview
This section provides a high level overview on the design of AP4K. The core building blocks are back-quoted and a definition
for them is provided in the next section (vocabulary).

AP4K provides multiple annotation processors all targeting at the generation of Kubernetes resources.
Each annotation processor is "supporting" one or more annotations and will be called by the compiler at least twice when
the annotations are spotted. 

On each annotation processor invocation, information about the annotated classes are passed.
The last invocation signals the end of the processing.

When the processing is over, each processor will assemble a `config` object and pass it to the responsible `handler`.
The handlers role is to create or modify the `model` based on the `config`. 

Finally, the `model` is serialized to disk as json/yml.

[sequence diagram][https://raw.githubusercontent.com/ap4k/ap4k/master/doc/src/main/resources/sequence.png]

## Vocabulary

The section below describes the core parts of AP4K and tries to describe how they work together.

### Visitor
Refers to the the `Gang of Four` visitor pattern. As Kubernetes resources are deeply nested we are extensively using the 
`visitor` pattern  to perform modifications to those resources without having to programmatically traverse this complex
structure.

### Model
With kubernetes/openshift `model` or just `model` we refer to the java object representation of the Kubernetes/Openshift
resource domain. The `model` has the same strcuture are the actual kubernetes resources and can be easily serialized into 
json or yml and form the actual resources.

### Config

An object / pojo that encapsulates the information provided by an annotation and the project (e.g. name, version etc).

### Configurator

A `configurator` is a visitor that visits parts of the `config` with the purpose of performing minor changes / updates.


| Configurator           | Target              | Description                                                                   |
|------------------------|---------------------|-------------------------------------------------------------------------------|
| ApplyOpenshiftConfig   | SourceToImageConfig | Applies group, name and version from OpenshiftConfig to SourceToImage config. |
| AddPort                | KubernetesConfig    | adds a port to all containers.                                                |
| ApplyDockerBuildHook   | DockerBuildConfig   | Apply the docker build hook configuration.                                    |
| ApplySourceToImageHook | SourceToImageConfig | Apply source to image build hook.                                             |


### Handler 

An object that can handle certain types of `config`. A `handler` may create resources and register `decorators`.

### Decorator

A `decorator` is a visitor that visits parts of the kubernetes/openshift manifest in order to pefrorm minor changes / updates.
It's different than a `configurator` in the sense that it operator on the actual model instead of the `config`.

| Decorator                     | Target         | Description                                            |
|-------------------------------|----------------|--------------------------------------------------------|
| AddSecretVolume               | PodSpec        | Add a secret volume to all pod specs.                  |
| AddService                    | KubernetesList | Add a service to the list.                             |
| AddLivenessProbe              | Container      | Add a liveness probe to all containers.                |
| AddEnvVar                     | Container      | Add a environment variable to the container.           |
| AddReadinessProbe             | Container      | Add a readiness probe to all containers.               |
| AddConfigMapVolume            | PodSpec        | Add a configmap volume to the pod spec.                |
| AddEnvToComponent             | ComponentSpec  | Add environment variable to component.                 |
| AddAzureDiskVolume            | PodSpec        | Add an Azure disk volume to the pod spec.              |
| AddAnnotation                 | ObjectMeta     | A decorator that adds an annotation to all resources.  |
| AddMount                      | Container      | Add mount to all containers.                           |
| AddServiceInstanceToComponent | ComponentSpec  | Add the service instance information to the component. |
| AddPort                       | Container      | Add port to all containers.                            |
| AddPvcVolume                  | PodSpec        | Add a persistent volume claim volume to all pod specs. |
| AddAwsElasticBlockStoreVolume | PodSpec        | Add an elastic block store volume to the pod spec.     |
| AddRuntimeToComponent         | ComponentSpec  | Add the runtime information to the component.          |
| AddLabel                      | ObjectMeta     | Add a label to the all metadata.                       |
| AddAzureFileVolume            | PodSpec        | Add an Azure File volume to the Pod spec.              |

### Annotation Processor
Refers to Java annotation processors. Each processor is responsible for creating a `config` object and also for registering one or more `handler` that handle the `config`.
A processor may register more than one `config` `handlers` with no restriction on the kind of `config` they handle. 

| Processor                         | Config               | Supported Annotations                                                                                        | Description                                                       |
|-----------------------------------|----------------------|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| SourceToImageAnnotationProcessor  | SourceToImageConfig  | [io.ap4k.openshift.annotation.SourceToImage]                                                                 | Adds source to image configuration in the openshift manifests.    |
| CompositeAnnotationProcessor      | CompositeConfig      | [io.ap4k.component.annotation.CompositeApplication]                                                          | Generate component custom resources.                              |
| SpringBootApplicationProcessor    | none                 | [org.springframework.boot.autoconfigure.SpringBootApplication]                                               | Detects Spring Boot and set the runtime attribute to Spring Boot. |
| KubernetesAnnotationProcessor     | KubernetesConfig     | [io.ap4k.annotation.KubernetesApplication]                                                                   | Generates kubernetes manifests.                                   |
| ThrorntailProcessor               | none                 | [javax.ws.rs.ApplicationPath, javax.jws.WebService]                                                          | Detects jaxrs and jaxws annotations and registers the http port.  |
| SpringBootMappingProcessor        | none                 | [org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.GetMapping] | Detects Spring Boot web endpoints and registers the http port.    |
| ServiceCatalogAnnotationProcessor | ServiceCatalogConfig | [io.ap4k.servicecatalog.annotation.ServiceCatalog, io.ap4k.servicecatalog.annotation.ServiceCatalogInstance] |                                                                   |
| OpenshiftAnnotationProcessor      | OpenshiftConfig      | [io.ap4k.annotation.KubernetesApplication, io.ap4k.openshift.annotation.OpenshiftApplication]                | Generates openshift manifests.                                    |
| MicronautProcessor                | none                 | [io.micronaut.http.annotation.Controller]                                                                    | Detects the micronaut controller and registers the http port.     |
| DockerBuildAnnotationProcessor    | DockerBuildConfig    | [io.ap4k.docker.annotation.DockerBuild]                                                                      | Register a docker build hook.                                     |

### Session
A shared repository between `annotation processors`. This repository holds `config`, `configurators`, `handlers`, `model` and `decorators`. When the session is closed, All `configurator` are applied, the final `config` is passed to the `handlers`.
The `handlers` generate and decorate the `model`. The resulting model is passed back to the `processor`.
