---
title: Service Binding
description: Service Binding
layout: docs
permalink: /docs/service-binding
---
### ServiceBinding CRD
[Service Binding Operator](https://github.com/redhat-developer/service-binding-operator) enables the application developers to bind the services that are backed by Kubernetes operators to an application that is deployed in kubernetes without having to perform manual configuration.
Dekorate supports generation of ServiceBinding CR.
The generation of ServiceBinding CR is triggered by annotating one of your classes with `@ServiceBinding` annotation and by adding the below dependency to the project and when the project gets compiled, the annotation will trigger the generation of ServiceBinding CR in both json and yml formats under the `target/classes/META-INF/dekorate`. The name of the ServiceBinding CR would be the name of the `applicationName + "-binding"`, for example if the application name is `sample-app`, the binding name would be `sample-app-binding`
```
<dependency>
  <groupId>io.dekorate</groupId>
  <artifactId>servicebinding-annotations</artifactId>
</dependency>
```
Here is the simple example of using ServiceBinding annotations in SpringBoot application.
```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.dekorate.servicebinding.annotation.Service;
import io.dekorate.servicebinding.annotation.ServiceBinding;
import io.dekorate.servicebinding.annotation.BindingPath;
@ServiceBinding(
  services = {
    @Service(group = "postgresql.dev", name = "demo-database", kind = "Database", version = "v1alpha1", id = "postgresDB") })
@SpringBootApplication
public class Main {
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```
For someone who wants to configure the ServiceBinding CR using system properties, they can do it in the application.properties. The ServiceBinding CR can be customized either via annotation parameters or via system properties. The parameter values provided via annotations can be overrided by configuring the ServiceBinding CR in application.properties.
```
dekorate.servicebinding.services[0].name=demo-database
dekorate.servicebinding.services[0].group=postgresql.dev
dekorate.servicebinding.services[0].kind=Database
dekorate.servicebinding.services[0].id=postgresDB
```
Generated ServiceBinding CR would look something like this:
```
apiVersion: operators.coreos.com/v1beta1
kind: ServiceBinding
metadata:
  name: servicebinding-binding-example
spec:
  application:
    group: apps
    resource: Deployment
    name: servicebinding-example
    version: v1
  services:
  - group: postgresql.dev
    kind: Database
    name: demo-database
    version: v1alpha1
    id: postgresDB
  detectBindingResources: false
  bindAsFiles: false
```
If the application's `bindingPath` needs to configured, `@BindingPath` annotation can be used directly under `@ServicingBinding` annotation. For example:
```
@ServiceBinding(
  bindingPath = @BindingPath(containerPath="spec.template.spec.containers")
  services = {
    @Service(group = "postgresql.dev", name = "demo-database", kind = "Database", version = "v1alpha1", id = "postgresDB") }, envVarPrefix = "postgresql")
@SpringBootApplication
```
**Note** : `ServiceBinding` annotations are already usuable though still highly experimental. The Service Binding operator is still in flux and may change in the near future.


#### External generator integration

No matter how good a generator/scaffolding tool is, its often desirable to handcraft part of it.
Other times it might be desirable to combine different tools together (e.g. to generate the manifests using fmp but customize them via dekorate annotations)

No matter what the reason is, dekorate supports working on existing resources and decorating them based on the provided annotation configuration.
This is as simple as letting dekorate know where to read the existing manifests and where to store the generated ones. By adding the [@GeneratorOptions](core/src/main/java/io/dekorate/annotation/GeneratorOptions.java).

##### Integration with Fabric8 Maven Plugin.

The fabric8-maven-plugin can be used to package applications for kubernetes and OpenShift. It also supports generating manifests.
A user might choose to build images using fmp, but customize them using `dekorate` annotations instead of xml.

An example could be to expose an additional port:

This can be done by configuring dekorate to read the fmp generated manifests
from `META-INF/fabric8` which is where fmp stores them and save them back
there once decoration is finished.
```java
@GeneratorOptions(inputPath = "META-INF/fabric8", outputPath = "META-INF/fabric8")
@KubernetesApplication(port = @Port(name="srv", containerPort=8181)
public class Main {
   ... 
}
```
#### related examples
- [spring boot with fmp on openshift example](examples/spring-boot-with-fmp-on-kubernetes-example)



