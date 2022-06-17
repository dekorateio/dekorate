---
title: Openshift
description: Openshift
layout: docs
permalink: /docs/openshift
---
### Openshift

## Setting up

Add the following dependency to your project:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>openshift-annotations</artifactId>
  <version>2.10.0</version>
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

## Building

    mvn clean package
    
The generated manifests can be found under `target/classes/META-INF/dekorate`.

## Configuration styles

The generated manifests can be customized either using annotations or configuration properties/yml.

### Using annotations

The `@OpenshiftApplication` under `io.dekorate.openshift.annotation` is a `@Dekorate` alternative through which
the user can specify all sorts of customization, For example to set the replicas to 2:

```java
package org.acme;

import io.dekorate.openshift.annotation.OpenshiftApplication;

@OpenshiftApplication(replicas=2)
public class Application {
}
```

### Using framework configuration

The same can be achieved using plain old configuration (e.g.`application.properties`):

```
dekorate.openshift.replicas=2
```

**A complete reference on all the supported properties can be found in the [configuration options guide]({{site.baseurl}}/configuration-guide).

#### Adding Kubernetes Jobs

To generate [Kubernetes Jobs](https://kubernetes.io/docs/concepts/workloads/controllers/job/), you can define them either using the `@OpenshiftApplication` annotation:
```java
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.Job;
import io.dekorate.openshift.annotation.OpenshiftApplication;

@OpenshiftApplication(jobs = @Job(name = "say-hello", containers = @Container(image = "docker.io/user/hello")))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```    

Or via configuration properties at the file `application.properties`:

    dekorate.openshift.jobs[0].name=say-hello
    dekorate.openshift.jobs[0].containers[0].image=docker.io/user/hello

Currently, the supported annotations for adding jobs are:

- @KubernetesApplication
- @OpenShiftApplication
- @KnativeApplication

#### Adding Kubernetes CronJobs

To generate [Kubernetes CronJobs](https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/), you can define them either using the `@OpenshiftApplication` annotation:
```java
@OpenshiftApplication(cronJobs = @CronJob(name = "say-hello", schedule = "* * * * *", containers = @Container(image = "docker.io/user/hello")))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```    

Or via configuration properties at the file `application.properties`:

    dekorate.openshift.cron-jobs[0].name=say-hello
    dekorate.openshift.cron-jobs[0].schedule=* * * * *
    dekorate.openshift.cron-jobs[0].containers[0].image=docker.io/user/hello
Dekorate CronJobs configuration follows the [Kubernetes CronJobs specification](https://kubernetes.io/docs/reference/kubernetes-api/workload-resources/cron-job-v1/#CronJobSpec). 

If the user doesn't provide CronJob container image, the pod template image configuration will be used.
Currently, the supported annotations for adding jobs are:

- @KubernetesApplication
- @OpenShiftApplication
- @KnativeApplication
