#Example:  Add configmap data to a volume without annotations in Spring Boot Applications 

An example that showcases how to generate a kubernetes resource utilizing Spring Boot-specific metadata. 
To customize the generated manifests you need to add dekorate properties to your `application.yml` or `application.properties` descriptors. 
and have the following dependency in your class path:

    <dependency>
          <groupId>io.dekorate</groupId>
          <artifactId>dekorate-spring-boot</artifactId>
          <version>${project.version}</version>
    </dependency>

The following annotation configuration:

```
@KubernetesApplication(group = "hello-world", labels = @Label(key = "foo", value = "bar"),
  ports = @Port(name = "http", containerPort = 8080),
  serviceType = ServiceType.LoadBalancer,
  configMapVolumes = @ConfigMapVolume(volumeName = "bar-volume", configMapName = "foo-map"))
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```
Can be expressed using properties:
```
dekorate.kubernetes.group=hello-world
dekorate.kubernetes.labels[0].key=foo
dekorate.kubernetes.labels[0].value=bar
dekorate.kubernetes.ports[0].name=http
dekorate.kubernetes.ports[0].containerPort=8080
dekorate.kubernetes.serviceType=LoadBalancer
dekorate.kubernetes.configMapVolumes[0].volumeName=bar-volume
dekorate.kubernetes.configMapVolumes[0].configMapName=foo-map

```

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/dekorate/kubernetes.yml` that should look like:
```---
   apiVersion: "apps/v1"
     kind: "Deployment"
     metadata:
       labels:
         app: "spring-boot-on-kubernetes-without-annotations"
         foo: "bar"
         version: "0.8-SNAPSHOT"
         food: "baz"
         group: "hello-world"
       name: "spring-boot-on-kubernetes-without-annotations"
     spec:
       replicas: 1
       selector:
         matchLabels:
           app: "spring-boot-on-kubernetes-without-annotations"
           foo: "bar"
           version: "0.8-SNAPSHOT"
           food: "baz"
           group: "hello-world"
       template:
         metadata:
           labels:
             app: "spring-boot-on-kubernetes-without-annotations"
             foo: "bar"
             version: "0.8-SNAPSHOT"
             food: "baz"
             group: "hello-world"
         spec:
           containers:
           - env:
             - name: "KUBERNETES_NAMESPACE"
               valueFrom:
                 fieldRef:
                   fieldPath: "metadata.namespace"
             image: "hello-world/spring-boot-on-kubernetes-without-annotations:0.8-SNAPSHOT"
             imagePullPolicy: "IfNotPresent"
             name: "spring-boot-on-kubernetes-without-annotations"
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
