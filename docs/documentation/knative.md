---
title: Knative
description: Knative
layout: docs
permalink: /docs/knative
---


### Knative

Dekorate also supports generating manifests for `knative`. To make use of
this feature you need to add:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>knative-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

This module provides the
[@KnativeApplication](annotations/knative-annotations/src/main/java/io/dekorate/knative/annotation/Knative.java) works exactly like  [@KubernetesApplication](annotations/kubernetes-annotations/src/main/java/io/dekorate/kubernetes/annotation/KubernetesApplication.java) , but will generate resources in a file name `knative.yml` / `knative.json` instead.
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

#### Adding Kubernetes Jobs

To generate [Kubernetes Jobs](https://kubernetes.io/docs/concepts/workloads/controllers/job/), you can define them either using the `@OpenshiftApplication` annotation:
```java
import io.dekorate.kubernetes.annotation.Container;
import io.dekorate.kubernetes.annotation.Job;
import io.dekorate.knative.annotation.KnativeApplication;

@KnativeApplication(jobs = @Job(name = "say-hello", containers = @Container(image = "docker.io/user/hello")))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```    

Or via configuration properties at the file `application.properties`:

    dekorate.knative.jobs[0].name=say-hello
    dekorate.knative.jobs[0].containers[0].image=docker.io/user/hello

Currently, the supported annotations for adding jobs are:

- @KubernetesApplication
- @OpenShiftApplication
- @KnativeApplication

#### Adding Kubernetes CronJobs

To generate [Kubernetes CronJobs](https://kubernetes.io/docs/concepts/workloads/controllers/cron-jobs/), you can define them either using the `@KnativeApplication` annotation:
```java
@KnativeApplication(cronJobs = @CronJob(name = "say-hello", schedule = "* * * * *", containers = @Container(image = "docker.io/user/hello")))
public class Main {

  public static void main(String[] args) {
    //Your code goes here
  }
}
```    

Or via configuration properties at the file `application.properties`:

    dekorate.knative.cron-jobs[0].name=say-hello
    dekorate.knative.cron-jobs[0].schedule=* * * * *
    dekorate.knative.cron-jobs[0].containers[0].image=docker.io/user/hello
Dekorate CronJobs configuration follows the [Kubernetes CronJobs specification](https://kubernetes.io/docs/reference/kubernetes-api/workload-resources/cron-job-v1/#CronJobSpec). 

If the user doesn't provide CronJob container image, the pod template image configuration will be used.
Currently, the supported annotations for adding jobs are:

- @KubernetesApplication
- @OpenShiftApplication
- @KnativeApplication
