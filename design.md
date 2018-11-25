# AP4K Design

## Terminology

The section below descibes the core parts of AP4K and tries to describe how they work together.

### Config

An object / pojo that encapsulates the information provided by an annotation and the project (e.g. name, version etc).

### Configurator

A `configurator` is a visitor that visits parts of the `configuration` with the purpose of performing minor changes / updates.


| Configurator           | Target              | Description                                                                   |
|------------------------|---------------------|-------------------------------------------------------------------------------|
| ApplyOpenshiftConfig   | SourceToImageConfig | Applies group, name and version from OpenshiftConfig to SourceToImage config. |
| AddPort                | KubernetesConfig    | adds a port to all containers.                                                |
| ApplyDockerBuildHook   | DockerBuildConfig   | Apply the docker build hook configuration.                                    |
| ApplySourceToImageHook | SourceToImageConfig | Apply source to image build hook.                                             |


### Handler 

An object that can handle certain types of `config`. A `processor` may create resources and register `decorators`.

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
Refers to Java annotation processors. Each processor is responsible for creating a `config` object and also for registering one or more `processors` that handle the `config`.
An processor may register more than one `config` processors with no restriction on the kind of `config` they handle. 

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
