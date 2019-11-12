# Test cases

## Annotation mode

This section describes some basic use cases of using `dekorate` annotations.

### Platforms

#### Kubernetes

When adding `kubernetes-annotations` to a generic java project no resource should be generated. (see #130:  [empty generic java projects](tests/annotations/kubernetes/src/it/issue-139-empty-generic-java-project)).

###### Basic options

When adding `kubernetes-annotations` to a project and annotating with the `Dekorate` or `KubernetesApplication` annotation, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. The manifests should contain at least `Deployment`.

When adding `kubernetes-annotations` to a project and annotating with the `KubernetesApplication(name="some-name")` annotation, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `app=some-name`.  The manifests should contain at least `Deployment` named `some-name`. The deployment should contain a single container also called `some-name`. The container should use the docker image `<user name>/some-name:<app version>`. If the name option is ommitted the name is expected to be the artifactId.

When adding `kubernetes-annotations` to a project and annotating with the `KubernetesApplication(group="some-group")` annotation, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `group=some-group`. The container should use the docker image `some-group/<app name>:<app version>`. If the name option is ommitted the group should default to the operating system user name.

When adding `kubernetes-annotations` to a project and annotating with the `KubernetesApplication(version="x.y.z")` annotation, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `version=x.y.z`. The container should use the docker image `<user name>/<app name>:x.y.z`. If the name option is ommitted the version should default to the application version.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(labels=@Label(key="foo", value="bar")")` annotation, I expect all generated resources to be labeled with `foo=bar`.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(annotations=@Annotation(key="foo", value="bar")")` annotation, I expect all generated resources to be annotated with `foo=bar`.

###### Environment variables

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(envVars=@EnvVar(name="FOO", value="BAR")")` annotation, I expect the generated container to have the environment variable `FOO=BAR`.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(envVars=@EnvVar(name="FOO", configmap="my-config", value="foo")")` annotation, I expect the generated container to have an environment varialbe named `FOO` that references config map's `my-config` field `foo`.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(envVars=@EnvVar(name="FOO", secret="my-secret", value="foo")")` annotation, I expect the generated container to have an environment varialbe named `FOO` that references secret's `my-secret` field `foo`.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(envVars=@EnvVar(name="FOO", field="spec.serviceAccountName")")` annotation, I expect the generated container to have an environment varialbe named `FOO` that references the field `spec.serviceAccountName`.

###### Ports

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(ports=@Port(name="my-port", containerPort=8181))` annotation, I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. 

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(ports={@Port(name="my-port", containerPort=8181), @Port(name="other-port", containerPort=8282))` annotation, I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. The service is expected to refer to both ports.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(expose=true)` annotation and also a port is defined (see above) or detected (provided known framework e.g. Spring Boot or Thorntail), I expect the generated manifest to contain an `Ingress`.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(expose=true)` annotation and also a port is defined (see above) or detected (provided known framework e.g. Spring Boot or Thorntail), I expect the generated manifest to contain an `Ingress`.

###### Volumes and mounts

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(configMapVolumes=@ConfigMapVolume(volumeName="my-vol", configMapName="my-config"))` annotation, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(configMapVolumes=@ConfigMapVolume(volumeName="my-vol", configMapName="my-config"), mounts=@Mount(name="my-vol", path="/some/path"))` annotation, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map. The generated volume should be mount to the container under `/some/path`.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(secretVolumes=@SecretVolume(volumeName="my-vol", secretName="my-config"))` annotation, I expect the pod to have a secret volume named `my-vol` that points to `my-secret` secret.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(secretVolumes=@SecretVolume(volumeName="my-vol", secretName="my-config"), mounts=@Mount(name="my-vol", path="/some/path"))` annotation, I expect the pod to have a secret volume named `my-vol` that points to `my-secret` secret. The generated volume should be mount to the container under `/some/path`.

In a similar manner the following kind of volumes should work.

###### Probes

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(livenessProbe=@Probe(httpActionPath="/foo"))` annotation, I expect the container to have a liveness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and failureThreshold 3 (defaults).

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(readinessProbe=@Probe(httpActionPath="/foo"))` annotation, I expect the container to have a readiness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and succss threshold 3 (defaults).

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(livenessProbe=@Probe(httpActionPath="/foo", initalDelaySeconds=10, periodSeconds=10, failureThreshold=5))` annotation, I expect the container to have a liveness probe that will perform an http call at `/foo` with period and initial delay of 10 seconds and failure threshold 5.

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(readinessProbe=@Probe(httpActionPath="/foo", initalDelaySeconds=10, periodSeconds=10, successThreshold=5))` annotation, I expect the container to have a liveness probe that will perform an http call at `/foo` with period and initial delay of 10 seconds and success threshold 5.

#### Openshift

###### Basic options

When adding `openshift-annotations` to a project and annotating with the `Dekorate` or `OpenshiftApplication` annotation, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. The manifests should contain at least `DeploymentConfig`, a `BuildConfig`.

When adding `openshift-annotations` to a project and annotating with the `OpenshiftApplication(name="some-name")` annotation, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `app=some-name`.  The manifests should contain at least `Deployment` named `some-name`. The deployment should contain a single container also called `some-name`. The container should use the docker image `<user name>/some-name:<app version>`. If the name option is ommitted the name is expected to be the artifactId.

When adding `openshift-annotations` to a project and annotating with the `OpenshiftApplication(group="some-group")` annotation, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `group=some-group`. The container should use the docker image `some-group/<app name>:<app version>`. If the name option is ommitted the group should default to the operating system user name.

When adding `openshift-annotations` to a project and annotating with the `OpenshiftApplication(version="x.y.z")` annotation, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `version=x.y.z`. The container should use the docker image `<user name>/<app name>:x.y.z`. If the name option is ommitted the version should default to the application version.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(labels=@Label(key="foo", value="bar")")` annotation, I expect all generated resources to be labeled with `foo=bar`.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(annotations=@Annotation(key="foo", value="bar")")` annotation, I expect all generated resources to be annotated with `foo=bar`.

###### Environment variables

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(envVars=@EnvVar(name="FOO", value="BAR")")` annotation, I expect the generated container to have the environment variable `FOO=BAR`.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(envVars=@EnvVar(name="FOO", configmap="my-config", value="foo")")` annotation, I expect the generated container to have an environment varialbe named `FOO` that references config map's `my-config` field `foo`.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(envVars=@EnvVar(name="FOO", secret="my-secret", value="foo")")` annotation, I expect the generated container to have an environment varialbe named `FOO` that references secret's `my-secret` field `foo`.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(envVars=@EnvVar(name="FOO", field="spec.serviceAccountName")")` annotation, I expect the generated container to have an environment varialbe named `FOO` that references the field `spec.serviceAccountName`.

###### Ports

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(ports=@Port(name="my-port", containerPort=8181))` annotation, I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. 

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(ports={@Port(name="my-port", containerPort=8181), @Port(name="other-port", containerPort=8282))` annotation, I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. The service is expected to refer to both ports.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(expose=true)` annotation and also a port is defined (see above) or detected (provided known framework e.g. Spring Boot or Thorntail), I expect the generated manifest to contain an `Ingress`.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(expose=true)` annotation and also a port is defined (see above) or detected (provided known framework e.g. Spring Boot or Thorntail), I expect the generated manifest to contain an `Ingress`.

###### Volumes and mounts

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(configMapVolumes=@ConfigMapVolume(volumeName="my-vol", configMapName="my-config"))` annotation, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(configMapVolumes=@ConfigMapVolume(volumeName="my-vol", configMapName="my-config"), mounts=@Mount(name="my-vol", path="/some/path"))` annotation, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map. The generated volume should be mount to the container under `/some/path`.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(secretVolumes=@SecretVolume(volumeName="my-vol", secretName="my-config"))` annotation, I expect the pod to have a secret volume named `my-vol` that points to `my-secret` secret.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(secretVolumes=@SecretVolume(volumeName="my-vol", secretName="my-config"), mounts=@Mount(name="my-vol", path="/some/path"))` annotation, I expect the pod to have a secret volume named `my-vol` that points to `my-secret` secret. The generated volume should be mount to the container under `/some/path`.

In a similar manner the following kind of volumes should work.

###### Probes

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(livenessProbe=@Probe(httpActionPath="/foo"))` annotation, I expect the container to have a liveness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and failureThreshold 3 (defaults).

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(readinessProbe=@Probe(httpActionPath="/foo"))` annotation, I expect the container to have a readiness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and succss threshold 3 (defaults).

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(livenessProbe=@Probe(httpActionPath="/foo", initalDelaySeconds=10, periodSeconds=10, failureThreshold=5))` annotation, I expect the container to have a liveness probe that will perform an http call at `/foo` with period and initial delay of 10 seconds and failure threshold 5.

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(readinessProbe=@Probe(httpActionPath="/foo", initalDelaySeconds=10, periodSeconds=10, successThreshold=5))` annotation, I expect the container to have a readiness probe that will perform an http call at `/foo` with period and initial delay of 5 seconds and success threshold 5.

## Annotationless mode

This section provides some basic use cases of using `dekorate` in annotationless mode.
It actually describes how all of the use cases provided so far map, to annotationless mode.

Note: Even though this mode is called `annotationless` in reality is `pseudo-annotationless` as it still requires an annotation to bootstrap the whole process.
Frameworks like [spring boot](https://spring.io/projects/spring-boot) or [thorntail](https://thorntail.io) which use known annotations don't need any code modification as `dekorate` can be bootstraped by those annotations.
Other frameworks or libraries (e.g. [vert.x](https://vertx.io) need to add the `Dekorate` annotation.)

The sections below will assume that either `Dekorate`, `KubernetesApplication` or `OpenshiftApplication` is added to the code, unless we are using a [spring boot](https://spring.io/projects/spring-boot) or [thorntail](https://thorntail.io) and the corresponding dependencies are present in the classpath.

### Platforms

#### Kubernetes

###### Basic options

When adding `kubernetes-annotations` to a project, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. The manifests should contain at least `Deployment`.

When adding `kubernetes-annotations` to a project and 
    
    dekorate.kubernetes.name=some-name
    
to application.properties, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `app=some-name`.  The manifests should contain at least `Deployment` named `some-name`. The deployment should contain a single container also called `some-name`. The container should use the docker image `<user name>/some-name:<app version>`. If the name option is ommitted the name is expected to be the artifactId.

When adding `kubernetes-annotations` to a project and 
    
    dekorate.kubernetes.group=some-group
    
to application.properties, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `group=some-group`. The container should use the docker image `some-group/<app name>:<app version>`. If the name option is ommitted the group should default to the operating system user name.

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.version=x.y.z 

to application.properties, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `version=x.y.z`. The container should use the docker image `<user name>/<app name>:x.y.z` . If the name option is ommitted the version should default to the application version.

When adding `kubernetes-annotations` to a project and

    
    dekorate.kubernetes.labels[0].key=foo
    dekorate.kubernetes.labels[0].value=bar
    
to application.properties, I expect all generated resources to be labeled with `foo=bar`.

When adding `kubernetes-annotations` to a project and 


    dekorate.kubernetes.annotations[0].key=foo
    dekorate.kubernetes.annotations[0].value=bar
    
to application.properties I expect all generated resources to be labeled with `foo=bar`.

###### Environment variables

When adding `kubernetes-annotations` to a project and

    dekorate.kubernetes.env-vars[0].name=FOO
    dekorate.kubernetes.env-vars[0].value=BAR
    
    
to application.properties, I expect all generated resources to be labeled with `FOO=BAR`.

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.env-vars[0].name=FOO
    dekorate.kubernetes.env-vars[0].value=BAR
    
to application.properties, I expect all generated resources to be labeled with `FOO=BAR`.

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.env-vars[0].name=FOO
    dekorate.kubernetes.env-vars[0].configmap=my-config
    dekorate.kubernetes.env-vars[0].value=FOO
    
to application.properties, I expect the generated container to have an environment varialbe named `FOO` that references config map's `my-config` field `foo`.

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.env-vars[0].name=FOO
    dekorate.kubernetes.env-vars[0].secret=my-secret
    dekorate.kubernetes.env-vars[0].value=FOO
    
to application.properties, I expect the generated container to have an environment varialbe named `FOO` that references secret's `my-secret` field `foo`.

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.env-vars[0].name=FOO
    dekorate.kubernetes.env-vars[0].field=spec.serviceAccountName
    
    
to application.properties, I expect the generated container to have an environment varialbe named `FOO` that references the field `spec.serviceAccountName`.

###### Ports

When adding `kubernetes-annotations` to a project and 


    dekorate.kubernetes.ports[0].name=my-port
    dekorate.kubernetes.ports[0].containerPort=8181
    
    
to application.properties , I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. 

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.ports[0].name=my-port
    dekorate.kubernetes.ports[0].containerPort=8181
    dekorate.kubernetes.ports[1].name=other-port
    dekorate.kubernetes.ports[1].containerPort=8282
    
to application.properties,  I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. The service is expected to refer to both ports. 

When adding `kubernetes-annotations` to a project and `dekorate.kubernetes.expose=true` to application.properties and also a port is defined (see above) or detected (provided known framework e.g. Spring Boot or Thorntail), I expect the generated manifest to contain an `Ingress`.

###### Volumes and mounts

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.config-map-volumes[0].volume-name=my-vol
    dekorate.kubernetes.config-map-volumes[0].config-map-name=my-config
    
to application.properties, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map.

When adding `kubernetes-annotations` to a project and
    
    dekorate.kubernetes.config-map-volumes[0].volume-name=my-vol
    dekorate.kubernetes.config-map-volumes[0].config-map-name=my-config
    
to application.properties, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map.

When adding `kubernetes-annotations` to a project and 

    dekorate.kubernetes.config-map-volumes[0].volume-name=my-vol
    dekorate.kubernetes.config-map-volumes[0].config-map-name=my-config
    dekorate.kubernetes.mounts[0].name=my-vol
    dekorate.kubernetes.mounts[0].path=/some/path

to application.properties, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map. The generated volume should be mount to the container under `/some/path`.

When adding `kubernetes-annotations` to a project and

    dekorate.kubernetes.secret-volumes[0].volume-name=my-vol
    dekorate.kubernetes.secret-volumes[0].secret-name=my-secret
    dekorate.kubernetes.mounts[0].name=my-vol
    dekorate.kubernetes.mounts[0].path=/some/path

to application.properties, I expect the pod to have a secret volume named `my-vol` that points to `my-secret` secret.

In a similar manner the following kind of volumes should work.

###### Probes

When adding `kubernetes-annotations` to a project and

    dekorate.kubernetes.liveness-probe.http-action-path=/foo

to application.properties, I expect the container to have a liveness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and failureThreshold 3 (defaults).

When adding `kubernetes-annotations` to a project and

    dekorate.kubernetes.readiness-probe.http-action-path=/foo

to application.properties, I expect the container to have a liveness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and succss threshold 3 (defaults).

When adding `kubernetes-annotations` to a project and

    dekorate.kubernetes.liveness-probe.http-action-path=/foo
    dekorate.kubernetes.liveness-probe.inital-delay-seconds=5
    dekorate.kubernetes.liveness-probe.period-seconds=5
    dekorate.kubernetes.liveness-probe.failure-threshold=5

to application.properties, I expect the container to have a liveness probe that will perform an http call at `/foo` with period and initial delay of 5 seconds and failure threshold 5.

When adding `kubernetes-annotations` to a project and

    dekorate.kubernetes.readiness-probe.http-action-path=/foo
    dekorate.kubernetes.readiness-probe.inital-delay-seconds=5
    dekorate.kubernetes.readiness-probe.period-seconds=5
    dekorate.kubernetes.readiness-probe.success-threshold=5

to application.properties, I expect the container to have a readiness probe that will perform an http call at `/foo` with period and initial delay of 5 seconds and success threshold 5.


#### Openshift

###### Basic options

When adding `openshift-annotations` to a project, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. The manifests should contain at least `Deployment`.

When adding `openshift-annotations` to a project and 
    
    dekorate.openshift.name=some-name
    
to application.properties, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `app=some-name`.  The manifests should contain at least `Deployment` named `some-name`. The deployment should contain a single container also called `some-name`. The container should use the docker image `<user name>/some-name:<app version>`. If the name option is ommitted the name is expected to be the artifactId.

When adding `openshift-annotations` to a project and 
    
    dekorate.openshift.group=some-group
    
to application.properties, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `group=some-group`. The container should use the docker image `some-group/<app name>:<app version>`. If the name option is ommitted the group should default to the operating system user name.

When adding `openshift-annotations` to a project and 

    dekorate.openshift.version=x.y.z 

to application.properties, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `version=x.y.z`. The container should use the docker image `<user name>/<app name>:x.y.z` . If the name option is ommitted the version should default to the application version.

When adding `openshift-annotations` to a project and

    
    dekorate.openshift.labels[0].key=foo
    dekorate.openshift.labels[0].value=bar
    
to application.properties, I expect all generated resources to be labeled with `foo=bar`.

When adding `openshift-annotations` to a project and 


    dekorate.openshift.annotations[0].key=foo
    dekorate.openshift.annotations[0].value=bar
    
to application.properties I expect all generated resources to be labeled with `foo=bar`.

###### Environment variables

When adding `openshift-annotations` to a project and

    dekorate.openshift.env-vars[0].name=FOO
    dekorate.openshift.env-vars[0].value=BAR
    
    
to application.properties, I expect all generated resources to be labeled with `FOO=BAR`.

When adding `openshift-annotations` to a project and 

    dekorate.openshift.env-vars[0].name=FOO
    dekorate.openshift.env-vars[0].value=BAR
    
to application.properties, I expect all generated resources to be labeled with `FOO=BAR`.

When adding `openshift-annotations` to a project and 

    dekorate.openshift.env-vars[0].name=FOO
    dekorate.openshift.env-vars[0].configmap=my-config
    dekorate.openshift.env-vars[0].value=FOO
    
to application.properties, I expect the generated container to have an environment varialbe named `FOO` that references config map's `my-config` field `foo`.

When adding `openshift-annotations` to a project and 

    dekorate.openshift.env-vars[0].name=FOO
    dekorate.openshift.env-vars[0].secret=my-secret
    dekorate.openshift.env-vars[0].value=FOO
    
to application.properties, I expect the generated container to have an environment varialbe named `FOO` that references secret's `my-secret` field `foo`.

When adding `openshift-annotations` to a project and 

    dekorate.openshift.env-vars[0].name=FOO
    dekorate.openshift.env-vars[0].field=spec.serviceAccountName
    
    
to application.properties, I expect the generated container to have an environment varialbe named `FOO` that references the field `spec.serviceAccountName`.

###### Ports

When adding `openshift-annotations` to a project and 


    dekorate.openshift.ports[0].name=my-port
    dekorate.openshift.ports[0].containerPort=8181
    
    
to application.properties , I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. 

When adding `openshift-annotations` to a project and 

    dekorate.openshift.ports[0].name=my-port
    dekorate.openshift.ports[0].containerPort=8181
    dekorate.openshift.ports[1].name=other-port
    dekorate.openshift.ports[1].containerPort=8282
    
to application.properties,  I expect the generated container to contain `my-port` container port and also expose it as a `Service` named after the deployment. The service is expected to refer to both ports. 

When adding `openshift-annotations` to a project and `dekorate.openshift.expose=true` to application.properties and also a port is defined (see above) or detected (provided known framework e.g. Spring Boot or Thorntail), I expect the generated manifest to contain an `Ingress`.

###### Volumes and mounts

When adding `openshift-annotations` to a project and 

    dekorate.openshift.config-map-volumes[0].volume-name=my-vol
    dekorate.openshift.config-map-volumes[0].config-map-name=my-config
    
to application.properties, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map.

When adding `openshift-annotations` to a project and
    
    dekorate.openshift.config-map-volumes[0].volume-name=my-vol
    dekorate.openshift.config-map-volumes[0].config-map-name=my-config
    
to application.properties, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map.

When adding `openshift-annotations` to a project and 

    dekorate.openshift.config-map-volumes[0].volume-name=my-vol
    dekorate.openshift.config-map-volumes[0].config-map-name=my-config
    dekorate.openshift.mounts[0].name=my-vol
    dekorate.openshift.mounts[0].path=/some/path

to application.properties, I expect the pod to have a config map volume named `my-vol` that points to `my-config` config map. The generated volume should be mount to the container under `/some/path`.

When adding `openshift-annotations` to a project and

    dekorate.openshift.secret-volumes[0].volume-name=my-vol
    dekorate.openshift.secret-volumes[0].secret-name=my-secret
    dekorate.openshift.mounts[0].name=my-vol
    dekorate.openshift.mounts[0].path=/some/path

to application.properties, I expect the pod to have a secret volume named `my-vol` that points to `my-secret` secret.

In a similar manner the following kind of volumes should work.

###### Probes

When adding `openshift-annotations` to a project and

    dekorate.openshift.liveness-probe.http-action-path=/foo

to application.properties, I expect the container to have a liveness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and failureThreshold 3 (defaults).

When adding `openshift-annotations` to a project and

    dekorate.openshift.readiness-probe.http-action-path=/foo

to application.properties, I expect the container to have a liveness probe that will perform an http call at `/foo` with period 30 seconds,  initial delay of 10 seconds and succss threshold 3 (defaults).

When adding `openshift-annotations` to a project and

    dekorate.openshift.liveness-probe.http-action-path=/foo
    dekorate.openshift.liveness-probe.inital-delay-seconds=5
    dekorate.openshift.liveness-probe.period-seconds=5
    dekorate.openshift.liveness-probe.failure-threshold=5

to application.properties, I expect the container to have a liveness probe that will perform an http call at `/foo` with period and initial delay of 5 seconds and failure threshold 5.

When adding `openshift-annotations` to a project and

    dekorate.openshift.readiness-probe.http-action-path=/foo
    dekorate.openshift.readiness-probe.inital-delay-seconds=5
    dekorate.openshift.readiness-probe.period-seconds=5
    dekorate.openshift.readiness-probe.success-threshold=5

to application.properties, I expect the container to have a readiness probe that will perform an http call at `/foo` with period and initial delay of 5 seconds and success threshold 5.

## Hybrid mode

Hybrid mode is using both annotations and application configuration.

When using hybrid mode it is expected that application configuration takes preceedence over annotation configuration on property level.
This means that each property defined both using annotations and configuration, the configuration property takes preceedence.
Note: `property level` means that when dealing with complex objects, overriding doesn't involve the whole object, but each of its properties individually.

### Kubernetes

When adding `kubernetes-annotations` to a project and annotating with the `KubernetesApplication(name="some-name")` annotation, but have:

    dekorate.kubernetes.name=some-other-name

in application.properties, I expect to find kubernetes manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `app=some-name`.  The manifests should contain at least `Deployment` named `some-other-name`. The deployment should contain a single container also called `some-other-name`. The container should use the docker image `<user name>/some-other-name:<app version>`. If the name option is ommitted the name is expected to be the artifactId.

The same applies for all simple properties.


###### Environment variables

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(envVars=@EnvVar(name="FOO", value="BAR")")` annotation,  but have:

    dekorate.kubernetes.env-vars[0].name=FOO
    dekorate.kubernetes.env-vars[0].value=BAR2

I expect the generated container to have the environment variable `FOO=BAR2`.

###### Ports

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(ports=@Port(name="my-port", containerPort=8181))` annotation, but have:


    dekorate.kubernetes.ports[0].name=my-port
    dekorate.kubernetes.ports[0].container-port=8282

I expect the generated container to contain `my-port` container port with port 8282 and also expose it as a `Service` named after the deployment. 

###### Volumes and mounts

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(configMapVolumes=@ConfigMapVolume(volumeName="my-vol", configMapName="my-config"))` annotation, but have:

    dekorate.kubernetes.config-map-volumes[0].volume-name=my-vol
    dekorate.kubernetes.config-map-volumes[0].config-map-name=my-other-config
    
I expect the pod to have a config map volume named `my-vol` that points to `my-other-config` config map.

In a similar manner the following kind of volumes should work.

###### Probes

When adding `kubernetes-annotations` to a project and annotating with `KubernetesApplication(livenessProbe=@Probe(httpActionPath="/foo", initalDelaySeconds=10, periodSeconds=10, failureThreshold=5))` annotation, but have:

    dekorate.kubernetes.liveness-probe.http-action-path=/bar
    
I expect the container to have a liveness probe that will perform an http call at `/bar` with period and initial delay of 10 seconds and failure threshold 5.


### Openshift

When adding `openshift-annotations` to a project and annotating with the `OpenshiftApplication(name="some-name")` annotation, but have:

    dekorate.openshift.name=some-other-name

in application.properties, I expect to find openshift manifests generated under `target/classes/META-INF/dekorate`. All resources are expected to contain the label `app=some-name`.  The manifests should contain at least `Deployment` named `some-other-name`. The deployment should contain a single container also called `some-other-name`. The container should use the docker image `<user name>/some-other-name:<app version>`. If the name option is ommitted the name is expected to be the artifactId.

The same applies for all simple properties.


###### Environment variables

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(envVars=@EnvVar(name="FOO", value="BAR")")` annotation,  but have:

    dekorate.openshift.env-vars[0].name=FOO
    dekorate.openshift.env-vars[0].value=BAR2

I expect the generated container to have the environment variable `FOO=BAR2`.

###### Ports

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(ports=@Port(name="my-port", containerPort=8181))` annotation, but have:


    dekorate.openshift.ports[0].name=my-port
    dekorate.openshift.ports[0].container-port=8282

I expect the generated container to contain `my-port` container port with port 8282 and also expose it as a `Service` named after the deployment. 

###### Volumes and mounts

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(configMapVolumes=@ConfigMapVolume(volumeName="my-vol", configMapName="my-config"))` annotation, but have:

    dekorate.openshift.config-map-volumes[0].volume-name=my-vol
    dekorate.openshift.config-map-volumes[0].config-map-name=my-other-config
    
I expect the pod to have a config map volume named `my-vol` that points to `my-other-config` config map.

In a similar manner the following kind of volumes should work.

###### Probes

When adding `openshift-annotations` to a project and annotating with `OpenshiftApplication(livenessProbe=@Probe(httpActionPath="/foo", initalDelaySeconds=10, periodSeconds=10, failureThreshold=5))` annotation, but have:

    dekorate.openshift.liveness-probe.http-action-path=/bar
    
I expect the container to have a liveness probe that will perform an http call at `/bar` with period and initial delay of 10 seconds and failure threshold 5.


