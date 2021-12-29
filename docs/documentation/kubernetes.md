---
title: Kubernetes
description: Kubernetes
layout: docs
permalink: /docs/kubernetes
---

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
  <version>{{site.data.project.release.current-version}}</version>
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

**REMINDER**: A complete reference on all the supported properties can be found in the [configuration options guide]({{site.baseurl}}/configuration-guide).

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
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

**Note**: The module is included in all starters.
