# Pojo to Custom Resource example (Zookeeper)

This is an imaginary yet realistic example of how a CustomResource for controlling Zookeeper instances could look like.
The example emphasizes on demonstrating the following features.

- Use of scalable resources
- Use mutliple versions
- Using `javax.validation.constraints.NotNul` to mark required fields.
- Specifying status subresource via annotations.
- Specifying scale attributes via annotations.
- Specifying additional printer columns via annotations



The `Zookeeper` pojo is annotated using the `CustomResource` annotation.

```java
@CustomResource(group = "io.zookeeper", version = "v1", storage=true, scope = Scope.Namespaced)
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


A second instance of the `Zookeeper` pojo is also provided and marked as version `v1alpha1`.

```java
@CustomResource(group = "io.zookeeper", version = "v1alpha1", scope = Scope.Namespaced)
public class Zookeeper {
  private ZookeeperSpec spec;
  @Status
  private ZookeeperStatus status;
}
```

Since two versions are present in the module, the crd generator will use them both.

## Required fields

In `v1` the `version` field of `ZookeeperSpec` is marked are `@NotNull`. This will be reflected in the generated open api schema and the field will be marked as required.

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

## Additional Printer Columns

In the `ZookeeperStatus` of `v1` fields `size` and `uptime` are also annotated as `@PrinterColumn`. The presence of the annotation will cause the generation of an `addtionalPrinerColumn` element containing a reference to those two fields.
