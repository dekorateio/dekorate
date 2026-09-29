package io.dekorate.generator.kubernetes.serviceaccount;

import io.dekorate.core.Configuration;
import io.dekorate.core.Generator;
import io.dekorate.core.VisitorRegistry;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;

public class ServiceAccountGenerator implements Generator {

  @Override
  public String getGroup() {
    return GROUP_SERVICE_ACCOUNT;
  }

  @Override
  public HasMetadata generate(Configuration config, VisitorRegistry registry) {
    String name = config.resolveString("dekorate.kubernetes.name");
    if (name == null) {
      name = "app";
    }

    ServiceAccountBuilder builder = new ServiceAccountBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata();

    registry.applyAll(builder, getGroup(), GROUP_COMMON);
    return builder.build();
  }
}
