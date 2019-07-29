# Knative Example 

A very simple example that demonstrates the use of `@KnativeApplication` in its simplest form.
Check the [Main.java](src/main/java/io/dekorate/examples/knative/Main.java) which bears the annotation.
To access the `@KnativeApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>knative-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/dekorate/knative.yml` that should look like:

    ---
    apiVersion: "v1"
    kind: "List"
    items:
    - apiVersion: "v1"
      kind: "Service"
      metadata:
        annotations: {}
        labels: {}
        name: "knative-example"
      spec:
        selector:
          app: "knative-example"
          version: "1.0-SNAPSHOT"
          group: "default"
        type: "ClusterIp"
    - apiVersion: "apps/v1"
      kind: "Deployment"
      spec:
        replicas: 1
        selector:
          matchLabels:
            app: "knative-example"
            version: "1.0-SNAPSHOT"
            group: "default"
        template:
          spec:
            containers:
            - env:
              - name: "KUBERNETES_NAMESPACE"
                valueFrom:
                  fieldRef:
                    fieldPath: "metadata.namespace"
              image: "knative-example:1.0-SNAPSHOT"
              imagePullPolicy: "IfNotPresent"
              name: "knative-example"
            nodeSelector: {} 
