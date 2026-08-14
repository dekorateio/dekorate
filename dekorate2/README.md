# Dekorate2 - A simplified visitor-based approach to enrich Kubernetes resources

## Motivation

Dekorate's core uses the Fabric8 `TypedVisitor<T>` pattern to modify Kubernetes resources. Each visitor targets a specific builder type (`ContainerBuilder`, `ObjectMetaBuilder`, etc.) and the Fabric8 builder tree dispatches `visit()` calls to matching types automatically.

However, the original approach requires:
- Manual wiring of each visitor in the code
- Passing raw values (strings, maps) to each visitor individually
- No unified configuration model
- No way for external JARs to contribute visitors without modifying the main code

Dekorate2 simplifies this by introducing:
1. A **configuration tree** built from a properties file with dotted key paths
2. A **visitor factory SPI** so visitors declare which config key they handle
3. A **visitor registry** that auto-discovers and applies matching visitors

## How it works

### Configuration tree

A standard `application.properties` file:

```properties
dekorate.kubernetes.image=quay.io/myorg/myapp:1.0
dekorate.kubernetes.labels.env=prod
dekorate.kubernetes.labels.team=backend
dekorate.kubernetes.replicas=3
```

Is parsed into a nested tree by `Configuration.fromProperties(path)`:

```
dekorate
  kubernetes
    image = "quay.io/myorg/myapp:1.0"
    labels
      env = "prod"
      team = "backend"
    replicas = "3"
```

Any node in the tree can be accessed with a dotted path:

```java
config.resolveString("dekorate.kubernetes.image");      // "quay.io/myorg/myapp:1.0"
config.resolveMap("dekorate.kubernetes.labels");         // {env=prod, team=backend}
```

### Visitor factory SPI

Each visitor implements `VisitorFactory`, declaring:
- **`getKeyPath()`** - which configuration key path it handles
- **`create(config)`** - produces a configured `TypedVisitor` instance

```java
public class SetImageVisitor<C extends Configuration> extends TypedVisitor<ContainerBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.image";

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new SetImageVisitor<>(config);
  }

  @Override
  public void visit(ContainerBuilder container) {
    String image = config.resolveString(KEY_PATH);
    if (image != null) {
      container.withImage(image);
    }
  }
}
```

### Visitor registry

`VisitorRegistry` handles discovery and application:

1. Loads visitor factories via `java.util.ServiceLoader` (finds implementations from all JARs on the classpath)
2. Accepts manual registration via `register(factory)` for programmatic use
3. For each factory, checks if its key path exists in the configuration
4. Applies matching visitors to the Kubernetes builder
5. Warns about configuration keys that have no registered visitor

```java
Configuration config = Configuration.fromProperties(Paths.get("application.properties"));

DeploymentBuilder builder = new DeploymentBuilder()
    .withNewMetadata().withName("my-app").endMetadata()
    // ... build the resource ...
    .endSpec();

VisitorRegistry registry = new VisitorRegistry(config)
    .register(new SetImageVisitor<>())
    .register(new AddLabelsVisitor<>());
registry.applyAll(builder);

Deployment deployment = builder.build();
```

### Logging output

The registry logs its activity via JBoss Logging:

```
INFO  ServiceLoader discovered 0 visitor factories
INFO  Applying visitor SetImageVisitor for key path: dekorate.kubernetes.image
INFO  Applying visitor AddLabelsVisitor for key path: dekorate.kubernetes.labels
INFO  Visitors applied: 2 out of 2 registered
WARN  No visitor registered for configuration key: dekorate.kubernetes.replicas
```

## Adding a new visitor

### 1. Create the visitor class

Create a class that extends `TypedVisitor<T>` and implements `VisitorFactory`:

```java
package io.dekorate.visitor;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpecBuilder;

public class SetReplicasVisitor<C extends Configuration> extends TypedVisitor<DeploymentSpecBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.replicas";
  private Configuration config;

  public SetReplicasVisitor() {}

  public SetReplicasVisitor(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() { return KEY_PATH; }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new SetReplicasVisitor<>(config);
  }

  @Override
  public void visit(DeploymentSpecBuilder spec) {
    Integer replicas = config.getInteger("replicas");
    if (replicas != null) {
      spec.withReplicas(replicas);
    }
  }
}
```

### 2. Register it

**Option A - ServiceLoader (for JARs on the classpath):**

Add the fully qualified class name to `META-INF/services/io.dekorate.core.VisitorFactory`:

```
io.dekorate.visitor.SetReplicasVisitor
```

The registry picks it up automatically - no code changes needed.

**Option B - Manual registration:**

```java
registry.register(new SetReplicasVisitor<>());
```

### 3. Add the configuration key

Add the corresponding property to `application.properties`:

```properties
dekorate.kubernetes.replicas=3
```

## Common TypedVisitor targets

| Type parameter | What you can modify |
|---|---|
| `ContainerBuilder` | Container image, ports, env vars, probes |
| `ObjectMetaBuilder` | Labels, annotations, name, namespace |
| `PodSpecBuilder` | Containers list, volumes, service account |
| `DeploymentSpecBuilder` | Replicas, strategy |
| `ServiceSpecBuilder` | Service ports, selectors, type |

## Generators

A `Generator` produces one Kubernetes resource. Each generator declares its **group** (e.g., `"deployment"`, `"service"`) and builds a skeleton resource using the Fabric8 builder API. It then asks the `VisitorRegistry` to apply all visitors matching its own group plus the `"common"` group.

### Generator interface

```java
public interface Generator {
  String getGroup();
  HasMetadata generate(Configuration config, VisitorRegistry registry);
}
```

Group constants are defined on the interface: `GROUP_DEPLOYMENT`, `GROUP_SERVICE`, `GROUP_SERVICE_ACCOUNT`, `GROUP_RBAC`, `GROUP_SECRET`, `GROUP_COMMON`.

### Available generators

| Generator | Group | Resource produced |
|---|---|---|
| `DeploymentGenerator` | `deployment` | `Deployment` with skeleton container |
| `ServiceGenerator` | `service` | `Service` with app selector |
| `ServiceAccountGenerator` | `serviceaccount` | `ServiceAccount` |
| `RbacGenerator` | `rbac` | `ClusterRoleBinding` bound to a `ClusterRole` and `ServiceAccount` |
| `SecretGenerator` | `secret` | `Secret` (Opaque) |

All generators read `dekorate.kubernetes.name` for the resource name (defaults to `"app"`).

### Visitor group discovery

Each `VisitorFactory` now declares a **group** via `getGroup()` (defaults to `"common"`). When a generator calls `registry.applyAll(builder, "deployment", "common")`, only visitors belonging to those groups are applied. This lets:
- **Common visitors** (labels, annotations) apply to every resource type
- **Resource-specific visitors** (image, replicas) apply only to their target generator

```
Generator ──► VisitorRegistry.applyAll(builder, ownGroup, "common")
                 │
                 ├── filters factories by group membership
                 ├── checks if config key path exists
                 └── applies matching visitors to the builder
```

## Session

The `Session` is the central orchestrator that ties together configuration, visitors, and generators.

### Components loaded by Session

1. **ConfigurationGenerator** — produces a `Configuration` from an external source. The default implementation `PropertiesConfigurationGenerator` reads a `.properties` file and builds the configuration tree
2. **Visitors** — discovered automatically via `ServiceLoader` through `VisitorRegistry`
3. **Generators** — discovered automatically via `ServiceLoader` (from `META-INF/services/io.dekorate.core.Generator`), or registered manually

### Lifecycle

The Session enforces a strict state machine: `CREATED → LOADED → STARTED → CLOSED`.

```java
try (Session session = new Session(configGenerator)) {
  session.load();                  // CREATED → LOADED: loads config, discovers visitors and generators
  session.register(customGen);     // optional manual registration (allowed before start)
  List<HasMetadata> resources = session.start();  // LOADED → STARTED: runs all generators
  // resources are available via session.getResources()
}
// close() exports resources as kubernetes.yml to the export path, then cleans up
```

The export path defaults to `./target` and can be configured:

```java
session.withExportPath(Paths.get("build/manifests"));
```

On `close()`, the Session writes all generated resources as a multi-document YAML file (`kubernetes.yml`) separated by `---` markers, ready for `kubectl apply -f`.

### Session listeners

Register a `SessionListener` to observe generator execution:

```java
session.addListener(new SessionListener() {
  @Override
  public void onGeneratorStarted(Generator generator) {
    // called before each generator runs
  }

  @Override
  public void onResourceGenerated(Generator generator, HasMetadata resource) {
    // called after each generator produces a resource
  }

  @Override
  public void onAllGenerated(List<HasMetadata> resources) {
    // called once all generators have completed
  }
});
```

All three methods are default, so listeners can override only the events they care about.

### How it works

```
Session
  ├── ConfigurationGenerator ──► Configuration (tree of properties)
  ├── VisitorRegistry        ──► discovers VisitorFactory SPIs
  └── Generator[]            ──► discovered via ServiceLoader
                                  each calls registry.applyAll(builder, group, "common")
                                  and returns a built HasMetadata resource

State machine:  CREATED ──load()──► LOADED ──start()──► STARTED ──close()──► CLOSED
                                       │                    │
                                  register()           getResources()
                                  withExportPath()     exportResources()
```

## Module structure

The project is split into two Maven modules under a parent pom:

- **`dekorate2-core`** — interfaces, SPI, session management (no Kubernetes-specific code)
- **`dekorate2-kubernetes`** — generators and decorators (depends on core)

Concrete visitor classes are named `xxxDecorate` (e.g., `AddLabelsDecorate`, `SetImageDecorate`) to reflect that they _decorate_ existing resources.

```
dekorate2/
  pom.xml                                         # Parent pom (modules: core, kubernetes)
  example/                                        # JBang scripts and sample config (see example/README.md)
    application.properties
    Fabric8BuilderWithTypedVisitor.java
    GenerateK8sResourcesUsingDekorateSession.java
    README.md
  core/
    pom.xml                                       # dekorate2-core
    src/main/java/io/dekorate/core/
      Configuration.java                          # Properties-to-tree parser with dotted path resolver
      ConfigurationGenerator.java                 # SPI interface for configuration sources
      PropertiesConfigurationGenerator.java       # Default impl: loads from .properties file
      Generator.java                              # Generator interface with group constants
      Session.java                                # Central orchestrator: config + visitors + generators
      SessionListener.java                        # Observer for generator execution events
      VisitorFactory.java                         # SPI interface with key path and group
      VisitorRegistry.java                        # ServiceLoader + manual registry, group-filtered apply
  kubernetes/
    pom.xml                                       # dekorate2-kubernetes (depends on core)
    src/main/java/io/dekorate/generator/kubernetes/
      common/
        AddLabelsDecorate.java                    # Adds labels (group: common)
        AddAnnotationsDecorate.java               # Adds annotations (group: common)
        SetNameDecorate.java                      # Sets resource name (group: common)
        SetNamespaceDecorate.java                 # Sets resource namespace (group: common)
      deployment/
        DeploymentGenerator.java                  # Produces Deployment
        SetImageDecorate.java                     # Sets container image (group: deployment)
      service/
        ServiceGenerator.java                     # Produces Service
      serviceaccount/
        ServiceAccountGenerator.java              # Produces ServiceAccount
      rbac/
        RbacGenerator.java                        # Produces ClusterRoleBinding
      secret/
        SecretGenerator.java                      # Produces Secret
    src/main/resources/META-INF/services/
      io.dekorate.core.Generator                  # ServiceLoader registration for generators
      io.dekorate.core.VisitorFactory             # ServiceLoader registration for decorators
```

## Examples

See the [example/](example/) folder for JBang scripts demonstrating the framework:

- **`GenerateK8sResourcesUsingDekorateSession.java`** — Session-based generation (recommended)
- **`Fabric8BuilderWithTypedVisitor.java`** — manual visitor wiring (legacy)

Quick start:

```bash
jbang dekorate2/example/GenerateK8sResourcesUsingDekorateSession.java dekorate2/example/application.properties
```

## TODO

- [x] Create Generator classes able to produce: Deployment, ServiceAccount, Service, RBAC, Secret. The Generator as we did within jbang should rely on f8 kube to create the builder accepting the list of the visitors associated. Implement toGo a discover mechanism able dynamically to find the  visitors and pass them to the builder. The vistors should be associated to a key path and/or family group to find them. Example: The kubernetes Deployment can be visited using the common visitor (enhancing the metadata), image, label, etc visitors according to the key x.y.z and/or group deployment
- [x] Create a Session able to load the Kubernetes Generators, different visitors discovered and ConfigurationGenerator
- [x] The default generator of the core package will load the different keys using the properties files
- [x] The Session must support to load, generate/visit the resources and when we close it to export the generated resources under a configurable export path. Default is: MANIFESTS created under ./target
- [x] Have a way to sync the Generators executed (maybe using a listener) to get at the end all the resources generated
- [x] Create a new jbang script using the Session to generate the resources
- [x] Move the Generator class and the key related visitors under the same package
- [x] Create a common generator package where you will handle the metadata which are common to all the resources generated such as labels, annotations, name, namespace, etc
- [x] Create under the module "dekorate2" a parent pom with 2 modules: core and kubernetes. Keep the code of session, interfaces, SPI under the core and move to kubernetes the generator, visitor classes. Rename the classes xxxVisitor to xxxDecorate.
- [ ] Propose a solution to bootstrap a session at compilation time if Dekorate is enabled. This is needed when we don't use 
