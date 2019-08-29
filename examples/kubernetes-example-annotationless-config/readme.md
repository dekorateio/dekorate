# Add configmap data to a volume Example 

An example that showcase how to generate a kubernetes resource utilizing Spring Boot-specific metadata. 
To customize the generated manifests you need to add dekorate properties to your `application.yml` or `application.properties` descriptors. 
and have the following dependency in your class path:

    <dependency>
          <groupId>io.dekorate</groupId>
          <artifactId>dekorate-spring-boot</artifactId>
          <version>${project.version}</version>
    </dependency>

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/dekorate/kubernetes.yml` that should look like:
```---
   apiVersion: "v1"
   kind: "List"
   items:
   - apiVersion: "v1"
     kind: "Service"
     metadata:
       labels:
         app: "kubernetes-example-annotationless-config"
         foo: "bar"
         version: "0.8-SNAPSHOT"
         food: "baz"
         group: "annotationless"
       name: "kubernetes-example-annotationless-config"
     spec:
       ports:
       - name: "http"
         port: 8080
         targetPort: 8080
       selector:
         app: "kubernetes-example-annotationless-config"
         foo: "bar"
         version: "0.8-SNAPSHOT"
         food: "baz"
         group: "annotationless"
       type: "LoadBalancer"
   - apiVersion: "apps/v1"
     kind: "Deployment"
     metadata:
       labels:
         app: "kubernetes-example-annotationless-config"
         foo: "bar"
         version: "0.8-SNAPSHOT"
         food: "baz"
         group: "annotationless"
       name: "kubernetes-example-annotationless-config"
     spec:
       replicas: 1
       selector:
         matchLabels:
           app: "kubernetes-example-annotationless-config"
           foo: "bar"
           version: "0.8-SNAPSHOT"
           food: "baz"
           group: "annotationless"
       template:
         metadata:
           labels:
             app: "kubernetes-example-annotationless-config"
             foo: "bar"
             version: "0.8-SNAPSHOT"
             food: "baz"
             group: "annotationless"
         spec:
           containers:
           - env:
             - name: "KUBERNETES_NAMESPACE"
               valueFrom:
                 fieldRef:
                   fieldPath: "metadata.namespace"
             image: "annotationless/kubernetes-example-annotationless-config:0.8-SNAPSHOT"
             imagePullPolicy: "IfNotPresent"
             name: "kubernetes-example-annotationless-config"
             ports:
             - containerPort: 8080
               name: "http"
               protocol: "TCP"
           volumes:
           - configMap:
               defaultMode: 384
               name: "foo-map"
               optional: false
             name: "bar-volume"

```
