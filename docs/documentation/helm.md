---
title: Helm
description: Helm
layout: docs
permalink: /docs/helm
---

### Helm

Dekorate also supports generating manifests for `helm`. To make use of this feature you need to add:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>helm-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

This module provides the [@HelmChart](annotations/helm-annotations/src/main/java/io/dekorate/helm/annotation/HelmChart.java) that will generate the following Helm resources:
- Chart.yaml
- values.yaml
- templates/*.yml the generated resources by Dekorate
- <chart name>-<chart version>-helm.tar.gz

#### Getting Started

Add your Helm chart configuration in your properties like:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart
```

Or annotate one of your Java source files with the Helm annotation:

```java
@HelmChart(name = "myChart", description = "Description of my Chart")
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```

#### Building

    mvn clean package

To generate the above Helm resources under the folder `target/classes/META-INF/dekorate/helm/<chart name>/`.

Moreover, assuming you're using Kubernetes, the Helm templates will include the following files by default:
- deployment.yaml
- ingress.yaml
- service.yaml
- NOTES.txt

#### Mapping Values

By default, Dekorate will generate the Helm values file (`values.yaml`) by mapping the following pre-configured properties:

- The Kubernetes/Openshift replicas
- The Kubernetes/Openshift image
- The Kubernetes/Openshift Env Var values (only for plain values - secrets or configmaps are not supported yet)
- The Kubernetes ingress host
- The Openshift S2i builder image

For example, if you set 3 replicas for your deployment:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Set replicas to 3
dekorate.kubernetes.replicas=3
```

Dekorate will generate the next Helm values file at `target/classes/META-INF/dekorate/helm/<chart name>/values.yaml`:

```yaml
---
myModule:
  replicas: 3
```

Note that `myModule` is the name of your project.

And the Deployment file at `target/classes/META-INF/dekorate/helm/<chart name>/templates/deployment.yaml` will have a reference to this value:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myModule
spec:
  replicas: {{ .Values.myModule.replicas }}
```

This is done transparently to users.

##### Mapping user properties using JSONPath expressions

What about if you want to map another property like the name of all the resources (the metadata name)?
Dekorate allows users to define [JSONPath](https://tools.ietf.org/id/draft-goessner-dispatch-jsonpath-00.html) expressions to map properties into the Helm values file.

For example, having the following YAML resource:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: helm-on-kubernetes-example
...
```

After getting familiar with JSONPath, the expression to map the metadata name value is `$..metadata.name`, so you need to add it to your configuration:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=myModule.name
dekorate.helm.values[0].jsonPaths=$..metadata.name
```

| Note that JSONPath works with JSON, not YAML. So, for testing your expressions, you first need to convert YAML files to JSON files using [this online tool](https://jsonformatter.org/yaml-to-json) and then use some JSONPath online evaluator like [https://jsonpath.com/](https://jsonpath.com/). Remember to wrap the json objects into lists.

The resulting `values.yaml` file will look like as:

```yaml
myModule:
  name: helm-on-kubernetes-example
```

Why the value is `helm-on-kubernetes-example`? This is because Dekorate will automatically inspect the generated resources to find the matching value of the JSONPath expression. 

However, users can provide other values using the `value` property:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=myModule.name
dekorate.helm.values[0].jsonPaths=$..metadata.name
dekorate.helm.values[0].value=this-is-another-name
```

And the `values.yaml` file will now contain:

```yaml
myModule:
  name: this-is-another-name
```

##### Mapping multiple properties at once

What about if the properties to map are at different locations, for example:

```yaml
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: helm-on-kubernetes-example ## we need to map this property
spec:
  rules:
    - host: my-host
      http:
        paths:
          - backend:
              service:
                name: helm-on-kubernetes-example ## And this property
                port:
                  name: http
            path: /
            pathType: Prefix
```

For this scenario, you need to provide a comma-separated list of JSONPath expressions to be mapped to the same property `myModule.name`:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=myModule.name
dekorate.helm.values[0].jsonPaths=$..metadata.name,$.[?(@.kind == 'Ingress')].spec.rules..http.paths..backend.service.name
```

So, Dekorate will first map the expression `$..metadata.name` and then the expression `$.[?(@.kind == 'Ingress')].spec.rules..http.paths..backend.service.name` (this expression only applies to `Ingress` resources - see more about filtering in JSONPath).

#### Helm Profiles

By default, all the properties are mapped to the same Helm values file `values.yaml`. However, Dekorate also supports the generation of Helm values by profiles. 
For example, let's say we have two environments: one for testing and another one for production; each environment have a different ingress host where your Kubernetes applications will be exposed. 
We can configure our application as:

```
dekorate.kubernetes.expose=true
# Mapped to `values.yaml` by the preconfigured Ingress decorator
dekorate.kubernetes.host=my-host

# Helm Chart
dekorate.helm.name=myChart
## Overwrite the value of `dekorate.kubernetes.host` to `values-<profile-name>.yaml`:
dekorate.helm.values[0].property=myModule.host
dekorate.helm.values[0].jsonPaths=$.[?(@.kind == 'Ingress')].spec.rules..host
dekorate.helm.values[0].value=my-test-host
dekorate.helm.values[0].profile=test
```

This configuration will generate the `values.yaml` using the property `dekorate.kubernetes.host`:

```yaml
myModule:
  host: my-host
```

But as you are now using a profile named `test` in one of your mapped properties, it will also generate a `values-test.yaml` file with the content:

```yaml
myModule:
  host: my-test-host
```

#### Helm Usage

First, make sure you have installed [the Helm command line](https://helm.sh/docs/intro/install/) and has access to a kubernetes cluster.

Then, run the following Maven command in order to generate the Helm artifacts and build/push the image into a container registry:

```shell
mvn clean package -Ddekorate.push=true -Ddekorate.docker.registry=<container registry url> -Ddekorate.docker.group=<your group>
```

This command will push the image into the container registry to be available for the cluster when deploying.

Finally, let's use Helm to deploy it into the cluster:

```shell
helm install helm-example ./target/classes/META-INF/dekorate/helm/<chart name>
```

The above command will use the default values, which are located in `./target/classes/META-INF/dekorate/helm/<chart name>/values.yaml`.
To override the default values, pass as parameter you own value file `--values /path/to/another.values.yaml` or set them using `--set key1=val1 --set key2=val2`.

How can I update my deployment?

- Via the `upgrade` option of Helm command line:

After making changes to your project, you would need to regenerate the resources using Dekorate:

```shell
mvn clean package -Ddekorate.push=true -Ddekorate.docker.registry=<container registry url> -Ddekorate.docker.group=<your group>
```

And then you need to upgrade your deployment:

```shell
helm upgrade helm-example ./target/classes/META-INF/dekorate/helm/<chart name>
```

- Via the `set` option of Helm command line:

```shell
helm upgrade helm-example ./target/classes/META-INF/dekorate/helm/<chart name> --set helmOnKubernetesExample.replicas=1
```

How can we delete my deployment?

```shell
helm uninstall helm-example
```
