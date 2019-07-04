# Service Catalog Example 

This example demonstrates the use of the `@ServiceCatalog` annotation.
Check the [Main.java](src/main/java/io/dekorate/examples/svcat/Main.java) which bears the annotation:

    @ServiceCatalog(instances = 
      @ServiceCatalogInstance(name = "mysql-instance", serviceClass = "apb-mysql", servicePlan = "default", bindingSecret = "mysql-secret")
    )

By adding this annotation the following things will happen:

- a `ServcieInstance` manifest for `mysql-instance` will be generated.
- a `ServiceBinding` manifest for `mysql-instance` will be generated.
- The deployment will be decorated with environment variables containing all binding information. 

The instance and the binding will look like:

    - apiVersion: "servicecatalog.k8s.io/v1beta1"
      kind: "ServiceBinding"
      metadata:
        name: "mysql-instance"
      spec:
         instanceRef:
          name: "mysql-instance"
        secretName: "mysql-secret"
    - apiVersion: "servicecatalog.k8s.io/v1beta1"
      kind: "ServiceInstance"
      metadata:
        name: "mysql-instance"
      spec:
        clusterServiceClassExternalName: "apb-mysql"
        clusterServicePlanExternalName: "default"

To access the `@ServiceCatalog` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>servicecatalog-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

Compile the project using:

    mvn clean install
