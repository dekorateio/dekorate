# Kubernetes Example 

A very simple example that demonstrates the use of `@KubernetesApplication` in its simplest form.

Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/ap4k/kubernetes.yml` that should look like:

    ---
    apiVersion: "v1"
    kind: "List"
    items:
    - apiVersion: "v1"
      kind: "Service"
      metadata:
        annotations: {}
        labels: {}
        name: "kubernetes-example"
      spec:
        selector:
          app: "kubernetes-example"
          version: "1.0-SNAPSHOT"
          group: "default"
        type: "ClusterIp"
    - apiVersion: "apps/v1"
      kind: "Deployment"
      spec:
        replicas: 1
        selector:
          matchLabels:
            app: "kubernetes-example"
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
              image: "kubernetes-example:1.0-SNAPSHOT"
              imagePullPolicy: "IfNotPresent"
              name: "kubernetes-example"
            nodeSelector: {} 
