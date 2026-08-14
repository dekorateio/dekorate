package io.dekorate.generator.kubernetes.deployment;

import io.dekorate.core.Configuration;
import io.dekorate.core.Generator;
import io.dekorate.core.VisitorRegistry;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

public class DeploymentGenerator implements Generator {

  @Override
  public String getGroup() {
    return GROUP_DEPLOYMENT;
  }

  @Override
  public HasMetadata generate(Configuration config, VisitorRegistry registry) {
    String name = config.resolveString("dekorate.kubernetes.name");
    if (name == null) {
      name = "app";
    }

    DeploymentBuilder builder = new DeploymentBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewSelector()
        .addToMatchLabels("app", name)
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", name)
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(name)
        .withImage("placeholder:latest")
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec();

    registry.applyAll(builder, getGroup(), GROUP_COMMON);
    return builder.build();
  }
}
