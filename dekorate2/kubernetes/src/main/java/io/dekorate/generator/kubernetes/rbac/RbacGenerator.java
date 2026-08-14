package io.dekorate.generator.kubernetes.rbac;

import io.dekorate.core.Configuration;
import io.dekorate.core.Generator;
import io.dekorate.core.VisitorRegistry;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBindingBuilder;

public class RbacGenerator implements Generator {

  @Override
  public String getGroup() {
    return GROUP_RBAC;
  }

  @Override
  public HasMetadata generate(Configuration config, VisitorRegistry registry) {
    String name = config.resolveString("dekorate.kubernetes.name");
    if (name == null) {
      name = "app";
    }

    ClusterRoleBindingBuilder builder = new ClusterRoleBindingBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewRoleRef()
        .withApiGroup("rbac.authorization.k8s.io")
        .withKind("ClusterRole")
        .withName(name)
        .endRoleRef()
        .addNewSubject()
        .withKind("ServiceAccount")
        .withName(name)
        .endSubject();

    registry.applyAll(builder, getGroup(), GROUP_COMMON);
    return builder.build();
  }
}
