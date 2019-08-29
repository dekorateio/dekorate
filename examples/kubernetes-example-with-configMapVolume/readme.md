# Add configmap data to a volume Example 

A very simple example that demonstrates the use of `@KubernetesApplication` in order to add ConfigMap data to a volume.
Check the [Main.java](src/main/java/io/dekorate/examples/kubernetes/Main.java) which bears the annotation.
To access the `@KubernetesApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/dekorate/kubernetes.yml` that should look like:
```---
   apiVersion: "v1"
   kind: "List"
   items:
   - apiVersion: "apps/v1"
     kind: "Deployment"
     metadata:
       labels:
         app: "kubernetes-example-with-configMapVolume"
         version: "0.8-SNAPSHOT"
         group: "amunozhe"
       name: "kubernetes-example-with-configMapVolume"
     spec:
       replicas: 1
       selector:
         matchLabels:
           app: "kubernetes-example-with-configMapVolume"
           version: "0.8-SNAPSHOT"
           group: "amunozhe"
       template:
         metadata:
           labels:
             app: "kubernetes-example-with-configMapVolume"
             version: "0.8-SNAPSHOT"
             group: "amunozhe"
         spec:
           containers:
           - env:
             - name: "KUBERNETES_NAMESPACE"
               valueFrom:
                 fieldRef:
                   fieldPath: "metadata.namespace"
             image: "amunozhe/kubernetes-example-with-configMapVolume:0.8-SNAPSHOT"
             imagePullPolicy: "IfNotPresent"
             name: "kubernetes-example-with-configMapVolume"
           volumes:
           - configMap:
               defaultMode: 384
               name: "foo-map"
               optional: false
             name: "bar-volume"
```
