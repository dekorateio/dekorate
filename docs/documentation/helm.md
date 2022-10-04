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

- The Kubernetes/OpenShift replicas
- The Kubernetes/OpenShift image
- The Kubernetes/OpenShift Env Var values (only for plain values - secrets or configmaps are not supported yet)
- The Kubernetes/OpenShift health checks for Readiness, Liveness and Startup probes
- The Kubernetes/OpenShift service type
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
app:
  replicas: 3
```

**Note**: By default, the extension will map all the properties under `app`. This can be modified using `dekorate.helm.alias`.  

And the Deployment file at `target/classes/META-INF/dekorate/helm/<chart name>/templates/deployment.yaml` will have a reference to this value:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myModule
spec:
  replicas: '{{ .Values.app.replicas }}'
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
dekorate.helm.values[0].property=name
dekorate.helm.values[0].paths=metadata.name
```

The resulting `values.yaml` file will look like as:

```yaml
app:
  name: helm-on-kubernetes-example
```

The `myModule.name` value is set automatically by Dekorate. However, users can provide other values using the `value` property:

```
dekorate.helm.name=myChart
dekorate.helm.description=Description of my Chart

# Map all the metadata name resources
dekorate.helm.values[0].property=name
dekorate.helm.values[0].paths=metadata.name
## Overwrite value:
dekorate.helm.values[0].value=this-is-another-name
```

And the `values.yaml` file will now contain:

```yaml
app:
  name: this-is-another-name
```

**What features do path expressions support?**

- Wildcard: map properties at any level

If we want to map a property that is placed at a very depth level, for example, the `containerPort` property:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: example
spec:
  replicas: 3
  selector:
    matchLabels:
      app.kubernetes.io/name: example
  template:
    metadata:
        app.kubernetes.io/name: example
    spec:
      containers:
        - env:
            - name: KUBERNETES_NAMESPACE
              valueFrom:
                fieldRef:
                  fieldPath: metadata.namespace
          name: example
          ports:
            - containerPort: 8080 // we want to map this property!
              name: http
              protocol: TCP
```

If we want to map the property `containerPort`, we would need to write all the parent properties as in:

```
dekorate.helm.values[0].paths=spec.template.spec.containers.ports.containerPort
```

And what about if the `containerPort` property is at one place in the Deployment resources, but at another place in the DeploymentConfig resources? We would need to provide two expressions. 

To ease up this use case, we can use wildcards. For example: 

```
## To map the container port property for containers with name "example"
dekorate.helm.values[0].paths=*.spec.containers.(name == example).ports.containerPort
## To map the container port property for all the resources at any position.
dekorate.helm.values[1].paths=*.ports.containerPort
```

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

- We can't use regular expressions.
- We can't write complex filters that involves AND/OR conditions. For example: the filter `(kind == Deployment && kind == DeploymentConfig || name == example)` is not supported.
- We can't select elements by index. For example, if we want to map the second container, we can't do: `spec.template.spec.containers.2.ports.containerPort`.

##### Using Helm expressions

We can also map the values using the exact Helm expression which allows using [Helm functions and pipelines](https://helm.sh/docs/chart_template_guide/functions_and_pipelines/).
To use it, we need to specify the `expression` when mapping the values, for example:

```
dekorate.helm.values[0].property=name
dekorate.helm.values[0].paths=metadata.name
dekorate.helm.values[0].expression={{ .Values.app.name | upper | quote }}
```

This expression will uppercase the value within the `app.name` and add the quotes. 

**NOTE:** If the expression is not provided, it will simply use `{{ .Values.<root alias>.<property> }}` which for the above example is `{{ .Values.app.name }}`.

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
dekorate.helm.values[0].property=name
## Comma separated list of JSONPath expressions:
dekorate.helm.values[0].jsonPaths=$..metadata.name,$.[?(@.kind == 'Ingress')].spec.rules..http.paths..backend.service.name
```

Now, Dekorate will first map the expression `$..metadata.name` and then the expression `$.[?(@.kind == 'Ingress')].spec.rules..http.paths..backend.service.name` (this expression only applies to `Ingress` resources - see more about filtering in JSONPath).

#### Helm Expressions Support

The Dekorate Helm extension partially supports Helm extensions via [Helm templates](https://helm.sh/docs/chart_template_guide/named_templates/) and [functions](https://helm.sh/docs/chart_template_guide/functions_and_pipelines/). You can make use of the templates and more complex functions using Helm expressions:

```properties
# Example of expressions
dekorate.helm.expressions[0].path=(kind == Service).metadata.annotations.'app.dekorate.io/commit-id'
dekorate.helm.expressions[0].expression={{ .Values.favorite.drink | default "tea" | quote }}

# Example of multiline expression
dekorate.helm.expressions[1].path=(kind == ConfigMap && metadata.name == my-configmap).data
dekorate.helm.expressions[1].expression={{- range $key, $val := .Values.favorite }}\n\
{{ indent 2 $key }}: {{ $val | quote }}\n\
{{- end }}
```

The Dekorate Helm extension will replace the specified path with the provided expression.

To provide your custom templates, you can add them into the folder `<input folder>/helm/templates/_helpers.tpl`, for example:

```
{{/*
Expand the name of the chart.
*/}}
{{- define "foo.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 5 }}
{{- end }}
```

And next, you can use this function using the Helm include primitive:

```properties
dekorate.helm.expressions[0].path=(kind == Service).metadata.annotations.'app.dekorate.io/build-timestamp'
dekorate.helm.expressions[0].expression={{ include "foo.name" . }}
```

Moreover, you can specify your Helm templates to only a concrete kind resource, for example, only for Service resources. To do this, you need to add the resource `<input folder>/helm/templates/<kind>.yaml` (following the example `<input folder>/helm/templates/service.yaml`). For example, the following resource will add two template functions called "mychart.labels" and "mychart.not-used":

```
{{- define "mychart.labels" }}
generator: helm
{{- end }}
{{- define "mychart.not-used" }}
not:
used: !
{{- end }}
```

And let's use the template "mychart.labels":

```properties
dekorate.helm.expressions[0].path=(kind == Service).metadata.labels
dekorate.helm.expressions[0].expression={{- template "mychart.labels" }}
```

#### Dependencies

Sometimes, your application requires of some other services to work. The typical scenario is when your application needs of a database to store the application data in. In this scenario, you need to declare the database service as a [Helm dependency](https://helm.sh/docs/helm/helm_dependency/). For example, let's declare [the Postgres Bitnami Helm](https://github.com/bitnami/charts/tree/master/bitnami/postgresql) dependency as database instance:

`Chart.yaml`:
```yaml
dependencies:
- name: postgresql
  version: 11.6.22
  repository: https://charts.bitnami.com/bitnami
  alias: database # this is optional. The default value is the `name`.
```

**IMPORTANT:** Before installing or packaging your Helm chart, you need to download the dependencies (you can use the Helm command `helm dependency update ./target/helm/kubernetes/<chart name>`).

Next, you can configure the dependencies adding the dependency configuration into the `values.yaml` file. For example, following the previous Postgres Bitnami dependency:

`values.yaml`:
```yaml
database: # the value in the `alias` property, or the `name` if unset.
  global:
    postgresql:
      auth:
        database: my_db_name
        postgresPassword: secret
```

Let's now see how you can add this configuration using the Dekorate Helm extension, so the `chart.yaml` and the `values.yaml` files are properly populated using a Helm dependency. You simply need to add the following properties:

`application.properties`:
```
dekorate.helm.dependencies[0].postgresql.alias=database
dekorate.helm.dependencies[0].postgresql.name=postgresql
dekorate.helm.dependencies[0].postgresql.version=11.6.22
dekorate.helm.dependencies[0].postgresql.repository=https://charts.bitnami.com/bitnami

dekorate.helm.values[0].property=database.global.postgresql.auth.postgresPassword
dekorate.helm.values[0].value=secret
dekorate.helm.values[1].property=postgresql.global.postgresql.auth.database
dekorate.helm.values[1].value=my_db_name
```

The Dekorate Helm extension will check whether the property set in `dekorate.helm.values.xxx.property` starts with a dependency alias or name. If so, it will use directly the value set. Otherwise, it will interpret that the property is an application property and will add the prefix set in the property `dekorate.helm.values-root-alias` (default value is `app`).

Alternatively, you can provide the properties of your dependency using your custom `values.yaml` file. You need to place this file at `src/main/helm` (the path is configurable using the property `dekorate.helm.inputFolder`).

`src/main/helm/values.yaml`:
```yaml
database: # the value in the `alias` property, or the `name` if unset.
  global:
    postgresql:
      auth:
        database: my_db_name
        postgresPassword: secret
```

This configuration will be aggregated in the autogenerated values file at `./target/classes/META-INF/dekorate/helm/<chart name>/values.yaml`.

#### Helm Profiles

By default, all the properties are mapped to the same Helm values file `values.yaml`. However, Dekorate also supports the generation of Helm values by profiles. 
For example, let's say we have two environments: one for testing and another one for production; each environment have a different ingress host where your Kubernetes applications will be exposed. 
We can configure our application as:

```
dekorate.kubernetes.ingress.expose=true
# Mapped to `values.yaml` by the preconfigured Ingress decorator
dekorate.kubernetes.ingress.host=my-host

# Helm Chart
dekorate.helm.name=myChart
## Overwrite the value of `dekorate.kubernetes.ingress.host` to `values-<profile-name>.yaml`:
dekorate.helm.values[0].property=host
dekorate.helm.values[0].jsonPaths=$.[?(@.kind == 'Ingress')].spec.rules..host
dekorate.helm.values[0].value=my-test-host
dekorate.helm.values[0].profile=test
```

This configuration will generate the `values.yaml` using the property `dekorate.kubernetes.ingress.host`:

```yaml
app:
  host: my-host
```

But as you are now using a profile named `test` in one of your mapped properties, it will also generate a `values-test.yaml` file with the content:

```yaml
app:
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
helm upgrade helm-example ./target/classes/META-INF/dekorate/helm/<chart name> --set app.replicas=1
```

How can we delete my deployment?

```shell
helm uninstall helm-example
```
