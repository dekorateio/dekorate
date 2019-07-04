# OpenShift Example 

A very simple example that demonstrates the use of `@OpenshiftApplication` in its simplest form.
Check the [Main.java](src/main/java/io/dekorate/examples/openshift/Main.java) which bears the annotation.
To access the `@OpenshiftApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/dekorate/openshfit.yml`.

    ---
    apiVersion: "v1"
    kind: "List"
    items:
    - apiVersion: "apps.openshift.io/v1"
      kind: "DeploymentConfig"
      metadata:
        name: "openshift-example"
      spec:
        replicas: 1
        selector:
          app: "openshift-example"
          version: "1.0-SNAPSHOT"
          group: "default"
        template:
          metadata:
            labels:
              app: "openshift-example"
              version: "1.0-SNAPSHOT"
              group: "default"
          spec:
            containers:
            - env:
              - name: "KUBERNETES_NAMESPACE"
                valueFrom:
                  fieldRef:
                    fieldPath: "metadata.namespace"
              - name: "JAVA_APP_JAR"
                value: "/deployments/openshift-example-1.0-SNAPSHOT.jar"
              image: ""
              imagePullPolicy: "IfNotPresent"
              name: "openshift-example"
        triggers:
        - imageChangeParams:
            automatic: true
            containerNames:
            - "openshift-example"
            from:
              kind: "ImageStreamTag"
              name: "openshift-example:1.0-SNAPSHOT"
          type: "ImageChange"
    
