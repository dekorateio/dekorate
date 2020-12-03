# Pojo to Custom Resource example (Zookeeper)

This is an imaginary yet realistic example of how a CustomResource for controlling Zookeeper instances could look like.
The example emphasizes on demonstrating the following features.

- Use of scalable resources
- Specifying status subresource via annotations.
- Specifying scale attributes via annotations.


The `Zookeeper` pojo is annotated using the `CustomResource` annotation.

```java
@CustomResource(group = "io.zookeeper", version = "v1", scope = Scope.Namespaced)
public class Zookeeper {
  private ZookeeperSpec spec;
  @Status
  private ZookeeperStatus status;
}
```

Using that annotation, the following property of the CRD are confugred:
- group
- version
- scope

## Subresources
The subresources are controlled using annotations.

### Status
By adding the `@Status` annotation on the status property, we let dekorate know that `ZookeeperStatus` is representing the custom resource status.

### Scale

The annotations:

- @SpecReplicas
- @StatusReplicas
- @LabelSelector

Mark which properties in the custom resource object graph hold the related information.

In particlar the property `size` in `ZookeeperSpec` and `ZookeeperStatus` hold the spec replicas and status replicas respctively. The property `labelSelector` in `ZookeeperStatus` holds the status label selector.

So the path that will be used to populate the Scale subresource are:

- spec replicas: `.spec.size`
- status replicas: `.status.size`
- label selector: `.status.labelSelector`
