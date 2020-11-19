# Pojo to Custom Resource example (Karaf)

This is an imaginary yet realistic example of how a CustomResource for controlling Karaf instances could look like.
The example emphasizes on demonstrating the following features.

- Showing one of the most simple use cases possible (non scalable, flat resource).
- Specifying status subresource via annotations.

The `Karaf` pojo is annotated using the `CustomResource` annotation.

```java
@CustomResource(group = "io.karaf", version = "v1", scope = Scope.Namespaced)
public class Karaf {
...
}
```

Using that annotation, the following property of the CRD are confugred:
- group
- version
- scope

## Subresources

The subresources in the example are controlled directly through the `@CustomResource` annotation.

### Status

The status class `KarafStatus` even though it's not part of the `Karaf` object graph is specified as status in the `@CustomResource` annotation.

```java
@CustomResource(group = "io.karaf", version = "v1", scope = Scope.Namespaced, status = KarafStatus.class)
public class Karaf {
...
}
```

By doing so, we 

```yaml
```
