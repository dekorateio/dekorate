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
4. **JBoss Logging** for visibility into what gets applied

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

## Project structure

```
dekorate2/
  application.properties                          # Sample configuration
  pom.xml                                         # Maven module (Fabric8 7.8.0, JBoss Logging)
  src/main/java/io/dekorate/
    EnrichK8sDeployment.java                      # JBang entry point
    core/
      Configuration.java                          # Properties-to-tree parser with dotted path resolver
      VisitorFactory.java                         # SPI interface for visitor discovery
      VisitorRegistry.java                        # ServiceLoader + manual registry, applies matching visitors
    visitor/
      SetImageVisitor.java                        # Sets container image from dekorate.kubernetes.image
      AddLabelsVisitor.java                       # Adds labels from dekorate.kubernetes.labels.*
  src/main/resources/
    META-INF/services/
      io.dekorate.core.VisitorFactory             # ServiceLoader registration file
```

## Running with JBang

```bash
jbang dekorate2/src/main/java/io/dekorate/EnrichK8sDeployment.java dekorate2/application.properties
```
