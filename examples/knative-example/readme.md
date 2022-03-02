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

## Integration testing

For the purpose of integration testing it includes:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>knative-junit</artifactId>
      <version>${project.version}</version>
      <scope>test</scope>
    </dependency>

This annotation will bring in the junit5 extension that dekorate provides, that allows you to run integration tests via the '@KnativeIntegrationTest' annotation.
The integration test is [KnativeExampleIT.java](src/test/java/io/dekorate/example/KnativeExampleIT.java) and it demonstrates:

- how you can deploy the application for end to end testing
- how use can use the kubernetes client from within the test to connect to the application.

The test are going to be automatically run when building the application. For example:

    mvn clean install

Note: To run the integration tests an actual kubernetes environment is required.
As the integration test requires a docker build to run, the tests will only run if an existing `Dockerfile` is found, and if the docker daemon used is the one use by kubernetes.
