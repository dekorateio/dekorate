package io.dekorate.generator.kubernetes.secret;

import io.dekorate.core.Configuration;
import io.dekorate.core.Generator;
import io.dekorate.core.VisitorRegistry;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.SecretBuilder;

public class SecretGenerator implements Generator {

  @Override
  public String getGroup() {
    return GROUP_SECRET;
  }

  @Override
  public HasMetadata generate(Configuration config, VisitorRegistry registry) {
    String name = config.resolveString("dekorate.kubernetes.name");
    if (name == null) {
      name = "app";
    }

    SecretBuilder builder = new SecretBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withType("Opaque");

    registry.applyAll(builder, getGroup(), GROUP_COMMON);
    return builder.build();
  }
}
