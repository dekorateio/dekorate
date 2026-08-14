# Dekorate2 examples

JBang scripts demonstrating dekorate-2 framework. Both scripts use the `application.properties` file in this folder as input.

## Prerequisites

- [JBang](https://www.jbang.dev/) installed

## application.properties

```properties
dekorate.kubernetes.name=my-app
dekorate.kubernetes.namespace=default

dekorate.kubernetes.image=quay.io/myorg/myapp:1.0

dekorate.kubernetes.labels.env=prod
dekorate.kubernetes.labels.team=backend
dekorate.kubernetes.labels.version=1.0

dekorate.kubernetes.annotations.description=My application

dekorate.kubernetes.replicas=3
```

## Fabric8BuilderWithTypedVisitor.java

Manually wires a `VisitorRegistry` and applies decorators to a hand-built `DeploymentBuilder`. No Session, no generators. Useful for understanding the low-level patterns.

What it does:
1. Reads configuration from the properties file
2. Builds a skeleton `Deployment` with the Fabric8 builder API
3. Manually registers `SetImageDecorate` and `AddLabelsDecorate`
4. Applies all matching visitors and prints the enriched Deployment as YAML

```bash
# From the dekorate2/example folder
jbang Fabric8BuilderWithTypedVisitor.java application.properties
```

Expected output:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    env: prod
    team: backend
    version: "1.0"
  name: my-app
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
        env: prod
        team: backend
        version: "1.0"
    spec:
      containers:
      - image: quay.io/myorg/myapp:1.0
        name: app
```

Only `SetImageDecorate` and `AddLabelsDecorate` are manually registered, so annotations, name, and namespace decorators are not applied. The name and namespace on the Deployment metadata come from the hand-built skeleton.

## GenerateK8sResourcesUsingDekorateSession.java (Session-based)

Uses the full `Session` lifecycle to generate Kubernetes resources. This is the recommended approach.

What it does:
1. Creates a `PropertiesConfigurationGenerator` from the config file
2. Opens a `Session` (auto-exports manifests on close)
3. Registers a `SessionListener` that prints each generated resource
4. Discovers decorators via `VisitorRegistry`, registers a `DeploymentGenerator`
5. Calls `session.start()` to generate resources and prints them as YAML
6. On close, exports a `kubernetes.yml` file to the export path

```bash
# From the project dekorate2/example
jbang GenerateK8sResourcesUsingDekorateSession.java application.properties build/manifests
```

Verify within `build/manifests` that we generated as resources: deployment, service, rbac, serviceaccount & secret

### Log trace

The framework logs each step , so you can follow what happens under the hood:

```
INFO  [io.dekorate.core.Session] Configuration loaded
INFO  [io.dekorate.core.VisitorRegistry] ServiceLoader discovered 5 visitor factories
INFO  [io.dekorate.core.Session] ServiceLoader discovered 5 generators
INFO  [io.dekorate.core.Session] Running generator DeploymentGenerator (group: deployment)
INFO  [io.dekorate.core.VisitorRegistry] Applying visitor SetNameDecorate for key path: dekorate.kubernetes.name (group: common)
INFO  [io.dekorate.core.VisitorRegistry] Applying visitor SetNamespaceDecorate for key path: dekorate.kubernetes.namespace (group: common)
INFO  [io.dekorate.core.VisitorRegistry] Applying visitor AddLabelsDecorate for key path: dekorate.kubernetes.labels (group: common)
INFO  [io.dekorate.core.VisitorRegistry] Applying visitor AddAnnotationsDecorate for key path: dekorate.kubernetes.annotations (group: common)
INFO  [io.dekorate.core.VisitorRegistry] Applying visitor SetImageDecorate for key path: dekorate.kubernetes.image (group: deployment)
INFO  [io.dekorate.core.VisitorRegistry] Visitors applied: 5 (groups: deployment, common)
INFO  [io.dekorate.core.Session] Running generator ServiceGenerator (group: service)
INFO  [io.dekorate.core.VisitorRegistry] Visitors applied: 4 (groups: service, common)
INFO  [io.dekorate.core.Session] Running generator ServiceAccountGenerator (group: serviceaccount)
INFO  [io.dekorate.core.VisitorRegistry] Visitors applied: 4 (groups: serviceaccount, common)
INFO  [io.dekorate.core.Session] Running generator RbacGenerator (group: rbac)
INFO  [io.dekorate.core.VisitorRegistry] Visitors applied: 4 (groups: rbac, common)
INFO  [io.dekorate.core.Session] Running generator SecretGenerator (group: secret)
INFO  [io.dekorate.core.VisitorRegistry] Visitors applied: 4 (groups: secret, common)
INFO  [io.dekorate.core.Session] Generated 5 resources
INFO  [io.dekorate.core.Session] Exported 5 resources to build/manifests/kubernetes.yml
INFO  [io.dekorate.core.Session] Session closed
```

The log shows:
- **Configuration loaded** -- the properties file was parsed into a configuration tree
- **ServiceLoader discovered 5** -- all generators and visitor factories found via `META-INF/services` on the classpath
- **Running generator** -- each generator runs in turn; the `DeploymentGenerator` gets 5 visitors (4 common + 1 deployment-specific `SetImageDecorate`), while the other generators get 4 common visitors each
- **Generated 5 resources** -- Deployment, Service, ServiceAccount, ClusterRoleBinding, Secret
- **Exported** -- on `close()`, the generated YAML was written to `build/manifests/kubernetes.yml`

### Expected YAML output

```yaml
Generated Deployment: default/my-app
Generated Service: default/my-app
Generated ServiceAccount: default/my-app
Generated ClusterRoleBinding: default/my-app
Generated Secret: default/my-app
---
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    description: My application
  labels:
    app: my-app
    env: prod
    team: backend
    version: "1.0"
  name: my-app
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      annotations:
        description: My application
      labels:
        app: my-app
        env: prod
        team: backend
        version: "1.0"
      name: my-app
      namespace: default
    spec:
      containers:
      - image: quay.io/myorg/myapp:1.0
        name: my-app
---
apiVersion: v1
kind: Service
metadata:
  annotations:
    description: My application
  labels:
    app: my-app
    env: prod
    team: backend
    version: "1.0"
  name: my-app
  namespace: default
spec:
  selector:
    app: my-app
---
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    description: My application
  labels:
    app: my-app
    env: prod
    team: backend
    version: "1.0"
  name: my-app
  namespace: default
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  annotations:
    description: My application
  labels:
    app: my-app
    env: prod
    team: backend
    version: "1.0"
  name: my-app
  namespace: default
roleRef:
  kind: ClusterRole
  apiGroup: rbac.authorization.k8s.io
  name: my-app
subjects:
- kind: ServiceAccount
  name: my-app
---
apiVersion: v1
kind: Secret
metadata:
  annotations:
    description: My application
  labels:
    app: my-app
    env: prod
    team: backend
    version: "1.0"
  name: my-app
  namespace: default
type: Opaque
```

All 5 generators produce resources. Common decorators (`SetNameDecorate`, `SetNamespaceDecorate`, `AddLabelsDecorate`, `AddAnnotationsDecorate`) apply to every resource, while `SetImageDecorate` only applies to the Deployment (group: `deployment`). On close, the YAML is exported to `build/manifests/kubernetes.yml`.

## apt-demo/ (compile-time generation)

A standalone Maven project that demonstrates the annotation processor approach. Unlike the JBang scripts above, this generates Kubernetes manifests automatically during `mvn compile` — no manual Session wiring needed.

The project uses `@EnableDekorate` on a simple Java class. The annotation processor bootstraps a Session at compile time, discovers all generators and visitors via ServiceLoader, and exports `kubernetes.yml` to `target/classes/META-INF/dekorate/`.

**Note:** `@EnableDekorate` is provided for POC usage. Consuming projects (e.g. Quarkus, Spring Boot integrations) should provide their own annotation processor rather than relying on this one.

### Prerequisites

The dekorate2 modules must be installed locally first:

```bash
# From the dekorate project root
mvn install -DskipTests -pl dekorate2/core,dekorate2/kubernetes,dekorate2/apt
```

### Trigger via annotation

The `MyApplication.java` class carries `@EnableDekorate`:

```java
@EnableDekorate
public class MyApplication {
  public static void main(String[] args) {
    System.out.println("Application started");
  }
}
```

```bash
cd dekorate2/example/apt-demo
mvn clean compile
```

### Trigger via system property (no annotation needed)

```bash
cd dekorate2/example/apt-demo
mvn clean compile -Ddekorate.enabled=true
```

Or via environment variable:

```bash
DEKORATE_ENABLED=true mvn clean compile
```

### Output

The manifest is generated at `target/classes/META-INF/dekorate/kubernetes.yml`:

```bash
cat target/classes/META-INF/dekorate/kubernetes.yml
```

```yaml
---
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    description: APT demo application
  labels:
    env: prod
    version: "1.0"
    team: backend
  name: apt-demo
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: apt-demo
  template:
    metadata:
      annotations:
        description: APT demo application
      labels:
        app: apt-demo
        env: prod
        version: "1.0"
        team: backend
      name: apt-demo
      namespace: default
    spec:
      containers:
      - image: quay.io/myorg/apt-demo:1.0
        name: apt-demo
---
apiVersion: v1
kind: Service
metadata:
  annotations:
    description: APT demo application
  labels:
    env: prod
    version: "1.0"
    team: backend
  name: apt-demo
  namespace: default
spec:
  selector:
    app: apt-demo
---
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    description: APT demo application
  labels:
    env: prod
    version: "1.0"
    team: backend
  name: apt-demo
  namespace: default
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  annotations:
    description: APT demo application
  labels:
    env: prod
    version: "1.0"
    team: backend
  name: apt-demo
  namespace: default
roleRef:
  kind: ClusterRole
  apiGroup: rbac.authorization.k8s.io
  name: apt-demo
subjects:
- kind: ServiceAccount
  name: apt-demo
---
apiVersion: v1
kind: Secret
metadata:
  annotations:
    description: APT demo application
  labels:
    env: prod
    version: "1.0"
    team: backend
  name: apt-demo
  namespace: default
type: Opaque
```

### Compiler log

During compilation you see the processor's activity:

```
INFO: Configuration loaded
INFO: ServiceLoader discovered 5 visitor factories
INFO: ServiceLoader discovered 5 generators
INFO: Running generator DeploymentGenerator (group: deployment)
INFO: Applying visitor AddLabelsDecorate for key path: dekorate.kubernetes.labels (group: common)
INFO: Applying visitor AddAnnotationsDecorate for key path: dekorate.kubernetes.annotations (group: common)
INFO: Applying visitor SetNameDecorate for key path: dekorate.kubernetes.name (group: common)
INFO: Applying visitor SetNamespaceDecorate for key path: dekorate.kubernetes.namespace (group: common)
INFO: Applying visitor SetImageDecorate for key path: dekorate.kubernetes.image (group: deployment)
INFO: Visitors applied: 5 (groups: deployment, common)
INFO: Running generator ServiceGenerator (group: service)
...
INFO: Generated 5 resources
INFO: Exported 5 resources to target/classes/META-INF/dekorate/kubernetes.yml
INFO: Session closed
[INFO] [dekorate2] Found @EnableDekorate on com.example.MyApplication
[INFO] [dekorate2] Manifests exported to target/classes/META-INF/dekorate
```

### How it works

The `pom.xml` declares `dekorate-2-apt` as a dependency and configures `annotationProcessorPaths` on the `maven-compiler-plugin` so that the processor, generators, and visitors are all on the annotation processor classpath:

```xml
<annotationProcessorPaths>
  <path>
    <groupId>io.dekorate</groupId>
    <artifactId>dekorate-2-apt</artifactId>
    <version>${dekorate2.version}</version>
  </path>
  <path>
    <groupId>io.dekorate</groupId>
    <artifactId>dekorate-2-kubernetes</artifactId>
    <version>${dekorate2.version}</version>
  </path>
  <path>
    <groupId>io.dekorate</groupId>
    <artifactId>dekorate-2-core</artifactId>
    <version>${dekorate2.version}</version>
  </path>
</annotationProcessorPaths>
```

This is required because `javac` uses a separate classpath for annotation processors. ServiceLoader inside the processor can only discover generators and visitors if their JARs (with `META-INF/services/` files) are on that path.
