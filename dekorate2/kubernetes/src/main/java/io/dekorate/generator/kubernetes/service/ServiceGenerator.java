package io.dekorate.generator.kubernetes.service;

import io.dekorate.core.Configuration;
import io.dekorate.core.Generator;
import io.dekorate.core.VisitorRegistry;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceBuilder;

public class ServiceGenerator implements Generator {

  @Override
  public String getGroup() {
    return GROUP_SERVICE;
  }

  @Override
  public HasMetadata generate(Configuration config, VisitorRegistry registry) {
    String name = config.resolveString("dekorate.kubernetes.name");
    if (name == null) {
      name = "app";
    }

    ServiceBuilder builder = new ServiceBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .addToSelector("app", name)
        .endSpec();

    registry.applyAll(builder, getGroup(), GROUP_COMMON);
    return builder.build();
  }
}
