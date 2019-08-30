# Add configmap data to a volume Example 

An example that demonstrates the use of `@KubernetesApplication` in order to add ConfigMap data to a volume.
To access the `@KubernetesApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>kubernetes-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

So as to add the ConfigMap under the volumes section of the Deployment specification you need pass the `configMapVolumes` parameter containing the volume and configMap names to the `@KubernetesApplication` in the Spring Boot annotated class. The code would look as follow:

```
@KubernetesApplication(configMapVolumes = @ConfigMapVolume(volumeName = "bar-volume", configMapName = "foo-map"))
@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}
```
Check, if necessary, the [Main.java](src/main/java/io/dekorate/examples/kubernetes/Main.java).

You can also specify the permission mode for the volume with the `defaultMode` parameter in `@ConfigMapVolume` annotation.
If you don’t specify any, [0600 is used by default](https://github.com/dekorateio/dekorate/blob/master/config.md#configmapvolume). Note that the JSON spec doesn’t support octal notation, so use the value 384 for 0600 permissions
Referenced `configMap` might be optional. By default this option will be false, so if `configMap` referenced is missing, kubernetes/openshift will lead to error. If `optional` parameter set to true, and referenced `configMap` not found, kubernetes/openshift will continue normally.

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/dekorate/kubernetes.yml` that should look like:
```---
   - apiVersion: "apps/v1"
     kind: "Deployment"
     metadata:
       labels:
         app: "kubernetes-example-with-configMapVolume"
         version: "0.8-SNAPSHOT"
       name: "kubernetes-example-with-configMapVolume"
     spec:
       replicas: 1
        template:
         metadata:
           labels:
             app: "kubernetes-example-with-configMapVolume"
             version: "0.8-SNAPSHOT"
         spec:
           volumes:
           - configMap:
               defaultMode: 384
               name: "foo-map"
               optional: false
             name: "bar-volume"
```
This deployment definition includes a volume `bar-volume` which references the `foo-map configMap`. Then, the `configMap` will have permission 0600.

