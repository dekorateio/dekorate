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
