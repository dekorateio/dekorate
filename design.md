# Dekorate Design

## Overview
This section provides a high level overview on the design of Dekorate. The core building blocks are back-quoted and a definition
for them is provided in the next section (vocabulary).

Dekorate provides multiple annotation processors all targeting at the generation of Kubernetes resources.
Each annotation processor is "supporting" one or more annotations and will be called by the compiler at least twice when
the annotations are spotted. 

On each annotation processor invocation, information about the annotated classes are passed.
The last invocation signals the end of the processing.

When the processing is over, each processor will assemble a `config` object and pass it to the responsible `handler`.
The handlers role is to create or modify the `model` based on the `config`. 

Finally, the `model` is serialized to disk as json/yml.

[![sequence diagram](https://raw.githubusercontent.com/dekorateio/dekorate/master/doc/src/main/resources/sequence.png)]

## Vocabulary

The section below describes the core parts of Dekorate and tries to describe how they work together.

### Visitor
Refers to the the `Gang of Four` visitor pattern. As Kubernetes resources are deeply nested we are extensively using the 
`visitor` pattern  to perform modifications to those resources without having to programmatically traverse this complex
structure.

### Model
With Kubernetes/OpenShift `model` or just `model` we refer to the java object representation of the Kubernetes/OpenShift
resource domain. The `model` has the same structure are the actual kubernetes resources and can be easily serialized into 
json or yml and form the actual resources.

The base interface that all `model` objects implement is the HasMetadata. For example:

     public class Pod implemnets HasMetadata {
     
       private String kind;
       private ObjectMeta metadata;
       
       public String getKind() {
         return this.kind;
       }
        
       public ObjectMeta getMetadata() {
          return this.metadata;
       }
     }

### Config

An object / pojo that encapsulates the information provided by an annotation and the project (e.g. name, version etc).
An example configuration is the `KubernetesConfig` class:

    public class KubernetecConfig  {
        private String group;
        private String name;
        private String version;
        private io.dekorate.kubernetes.config.Label[] labels;
        private io.dekorate.kubernetes.config.Annotation[] annotations;
        ...
    }

### Configurator

A `configurator` is a visitor that visits parts of the `config` with the purpose of performing minor changes / updates.

For example a configurator that can be used to add a label to `KubernetesConfig`:

    public class AddLabel extends Configurator<KubernetesConfigFluent> {
    
         public void visit(KubernetesConfigFluent config) {
             config.addToLabels(new Label("createdBy", "dekorate");
         }
    }


| Configurator           | Target              | Description                                                                   |
|------------------------|---------------------|-------------------------------------------------------------------------------|
| ApplyOpenshiftConfig   | SourceToImageConfig | Applies group, name and version from OpenshiftConfig to SourceToImage config. |
| AddPort                | KubernetesConfig    | adds a port to all containers.                                                |
| ApplyDockerBuildHook   | DockerBuildConfig   | Apply the docker build hook configuration.                                    |
| ApplySourceToImageHook | SourceToImageConfig | Apply source to image build hook.                                             |


### Handler 

An object that can handle certain types of `config`. A `handler` may create `model` resources and register `decorators`.
The handler is the object that does most of the creation of the `model` resources.

     public class KubernetesHandler extends AbstractKubernetesHandler<KubernetesConfig> {

        public void handle(KubernetesConfig config) {
            resources.add("kubernetes", createDeployment(config));
        }

        public boolean canHandle(Class<? extends Configuration> type) {
          return type.equals(KubernetesConfig.class);
        }
    }

### Decorator

A `decorator` is a visitor that visits parts of the kubernetes/openshift `model` in order to perform minor changes / updates.
It's different than a `configurator` in the sense that it operates on the actual model instead of the `config`.

    public class AddLabel extends Decorator<PodFluent> {
    
         public void vist(PodFluent podFluent) {
             podFluent.addToLabels(new Label("createdBy", "dekorate");
         }
    }
    
    
The `decorator` looks pretty similar to the `configurator` the only difference between the two being the kind of objects they visit.

`Configurators` visit `config` objects.
`Decorators` visit `model` objects.

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

#### Why do we need both Configurators and Decorators?

The kubernetes `model` is very complex and deeply nested object structure and for a good reason: `It needs to fit to every signle deployment use case out there.`
The deployment of a java application though is something more concrete and can be described by something simpler than the actual model.

During the process of gathering and combining information from multiple annotation processors its more practical and less error prone to apply them to a more simplified representation of the `model`.
which is what the `config` essentially is. So, during the processing phase we use `configurators` to apply the information gathered in each step to the `config`.

Once the `configuration` is finalized, the actual `model` is populated. Since different `processors` are creating different kinds of `config` we need to combine them all in order to build the `model`.
This is where `decorators` come in place. Each `config` is translated to different `decorators` that contribute to different parts of the `model`.

##### Why not directly creating the model?
We have a variable number of `config` instances all contributing to the `model`. Combining them all in one go without the use of `decorators` would result in a conditional hell.

### Annotation Processor
Refers to Java annotation processors. Each processor is responsible for creating a `config` object and also for registering one or more `handler` that handle the `config`.
A processor may register more than one `config` `handlers` with no restriction on the kind of `config` they handle. 

| Processor                         | Config               | Supported Annotations                                                                                        | Description                                                       |
|-----------------------------------|----------------------|--------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------|
| ComponentAnnotationProcessor      | CompositeConfig      | [io.dekorate.component.annotation.ComponentApplication]                                                          | Generate component custom resources.                              |
| LinkAnnotationProcessor           | LinkConfig           | [io.dekorate.component.annotation.Link]                                                                          | Generate link custom resources.                                   |
| SpringBootApplicationProcessor    | none                 | [org.springframework.boot.autoconfigure.SpringBootApplication]                                               | Detects Spring Boot and set the runtime attribute to Spring Boot. |
| KubernetesAnnotationProcessor     | KubernetesConfig     | [io.dekorate.kubernetes.annotation.KubernetesApplication]                                                        | Generates kubernetes manifests.                                   |
| ThrorntailProcessor               | none                 | [javax.ws.rs.ApplicationPath, javax.jws.WebService]                                                          | Detects jaxrs and jaxws annotations and registers the http port.  |
| SpringBootMappingProcessor        | none                 | [org.springframework.web.bind.annotation.RequestMapping, org.springframework.web.bind.annotation.GetMapping] | Detects Spring Boot web endpoints and registers the http port.    |
| ServiceCatalogAnnotationProcessor | ServiceCatalogConfig | [io.dekorate.servicecatalog.annotation.ServiceCatalog, io.dekorate.servicecatalog.annotation.ServiceCatalogInstance] |                                                                   |
| OpenshiftAnnotationProcessor      | OpenshiftConfig      | [io.dekorate.kubernetes.annotation.KubernetesApplication, io.dekorate.openshift.annotation.OpenshiftApplication]     | Generates openshift manifests.                                    |
| MicronautProcessor                | none                 | [io.micronaut.http.annotation.Controller]                                                                    | Detects the micronaut controller and registers the http port.     |

### Session
A shared repository between `annotation processors`. This repository holds `config`, `configurators`, `handlers`, `model` and `decorators`. When the session is closed, All `configurator` are applied, the final `config` is passed to the `handlers`.
The `handlers` generate and decorate the `model`. The resulting model is passed back to the `processor`.
