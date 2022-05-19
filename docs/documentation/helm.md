---
title: Helm
description: Helm
layout: docs
permalink: /docs/helm
---

### Helm

To let Dekorate to generate the Helm manifest files for a project, simply declare the following dependency part of your pom file:

```xml
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>helm-annotations</artifactId>
  <version>{{site.data.project.release.current-version}}</version>
</dependency>
```

This dependency will generate the following Helm resources:
- Chart.yaml
- values.yaml
- templates/*.yml the generated resources by Dekorate
- <chart name>-<chart version>-helm.tar.gz

#### Getting Started

To generate the Helm resources, you first need to configure the Helm chart via properties:

```
# This name property is mandatory to generate the Helm chart
dekorate.helm.name=myChart
# If the version is not provided, the application version will be used instead
dekorate.helm.version=1.0.0-SNAPSHOT
# The description property is optional
dekorate.helm.description=Description of my Chart
```

Or annotate one of your Java source files with the [@HelmChart](https://raw.githubusercontent.com/dekorateio/dekorate/main/annotations/helm-annotations/src/main/java/io/dekorate/helm/annotation/HelmChart.java) annotation:

```java
@HelmChart(name = "myChart", version = "1.0.0-SNAPSHOT", description = "Description of my Chart")
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```

Once, you have configured the Helm chart, you can generate the Helm resources under the folder `target/classes/META-INF/dekorate/helm/<chart name>/` using the maven command:

    mvn clean package

Depending on the Dekorate extensions that you are using in your project, the Helm resources will include some templates or others. For example, if your project declared the [Kubernetes]({{site.baseurl}}/docs/kubernetes) Dekorate extension, then the helm resources will include the following templates at `target/classes/META-INF/dekorate/helm/<chart name>/templates/`:
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

**Note**: `myModule` is the name of your project.

And the Deployment file at `target/classes/META-INF/dekorate/helm/<chart name>/templates/deployment.yaml` will have a reference to this value:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myModule
spec:
  replicas: '{{ .Values.myModule.replicas }}'
```

This is done transparently to users.

##### Mapping user properties using path expressions

As we have introduced in the previous section, Dekorate will automatically map some properties like the `replicas` or the `images` to the Values helm file. Still, some users might need to map more properties. For example, let's see the following YAML resource:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: helm-on-kubernetes-example
...
```

The property at `metadata.name` will not be replaced with `{{ .Values.myModule.name }}` in the Helm templates.
However, Dekorate allows users to define path expressions to map properties into the Helm values file. Let's see how to do it using the above example to map the property `metadata.name` with `{{ .Values.myModule.name }}`.

To build the right path you want to use, you simply need to loop over the YAML tree at the resource level:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: helm-on-kubernetes-example
...
```

Then, the expression to map the metadata name value is `metadata.name`, so you need to add it to your configuration:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=myModule.name
dekorate.helm.values[0].paths=metadata.name
```

The resulting `values.yaml` file will look like as:

```yaml
myModule:
  name: helm-on-kubernetes-example
```

The `myModule.name` value is set automatically by Dekorate. However, users can provide other values using the `value` property:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=myModule.name
dekorate.helm.values[0].paths=metadata.name
## Overwrite value:
dekorate.helm.values[0].value=this-is-another-name
```

And the `values.yaml` file will now contain:

```yaml
myModule:
  name: this-is-another-name
```

**What features do path expressions support?**

- Escape characters

If you want to select properties which key contains special characters like '.', you need to escape them using `'`, for example:

```
## To map the property only for Service resources:
dekorate.helm.values[0].paths=spec.selector.matchLabels.'app.kubernetes.io/name'
```

- Filter

If you want to only map properties of certain resource type, you can add as many conditions you need in the path such as:

```
## To map the property only for Service resources:
dekorate.helm.values[0].paths=(kind == Service).metadata.name
```

Additionally, we can write the filter including the "and" operator using "&&" or the "or" operator using "||": 

```
## To map the property only for Service resources AND resources that has an annotation 'key' with value 'some' 
dekorate.helm.values[0].paths=(kind == Service && metadata.annotations.'key' == 'some.text').metadata.name

## To map the property only for either Deployment OR DeploymentConfig resources 
dekorate.helm.values[1].paths=(kind == Deployment || kind == DeploymentConfig).metadata.name
```

Also, filters can be placed at any place in the path expression and also at multiple times. Let's see an example of this: we want to map the container port of containers with name `example` and only for Deployment resources:

```
## To map the property only for Deployment resource AND containers with a concrete name 
dekorate.helm.values[0].paths=(kind == Deployment).spec.template.spec.containers.(name == example).ports.containerPort
```

**What is not supported using path expressions?**

- We can't use wildcards or regular expressions.
- We can't write complex filters that involves AND/OR conditions. For example: the filter `(kind == Deployment && kind == DeploymentConfig || name == example)` is not supported.
- We can't select elements by index. For example, if we want to map the second container, we can't do: `spec.template.spec.containers.2.ports.containerPort`.

##### Mapping multiple properties at once

What about if the properties are located in different places, for example:

```yaml
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: helm-on-kubernetes-example ## (1)
spec:
  rules:
    - host: my-host
      http:
        paths:
          - backend:
              service:
                name: helm-on-kubernetes-example ## (2)
                port:
                  name: http
            path: /
            pathType: Prefix
```

From this example, we need to map the value `helm-on-kubernetes-example` which is used in two places: (1) `metadata.name` and (2) `spec.rules..http.paths..backend.service.name` to the same property `myModule.name`. For doing this, we need to provide a comma-separated list of JSONPath expressions to be mapped to `myModule.name`:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=myModule.name
## Comma separated list of JSONPath expressions:
dekorate.helm.values[0].jsonPaths=$..metadata.name,$.[?(@.kind == 'Ingress')].spec.rules..http.paths..backend.service.name
```

Now, Dekorate will first map the expression `$..metadata.name` and then the expression `$.[?(@.kind == 'Ingress')].spec.rules..http.paths..backend.service.name` (this expression only applies to `Ingress` resources - see more about filtering in JSONPath).

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

First, make sure you have installed [the Helm command line](https://helm.sh/docs/intro/install/) and connected/logged to a kubernetes cluster.

Then, run the following Maven command in order to generate the Helm artifacts and build/push the image into a container registry:

```shell
mvn clean package -Ddekorate.push=true -Ddekorate.docker.registry=<container registry url> -Ddekorate.docker.group=<your group>
```

This command will push the image to a container registry and will become available when a pod or container is created.

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
