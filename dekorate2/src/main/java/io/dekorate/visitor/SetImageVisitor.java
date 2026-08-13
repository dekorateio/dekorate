package io.dekorate.visitor;

import io.dekorate.core.Configuration;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class SetImageVisitor<C extends Configuration> extends TypedVisitor<ContainerBuilder> {

  private final Configuration config;

  public SetImageVisitor(Configuration config) {
    this.config = config;
  }

  @Override
  public void visit(ContainerBuilder container) {
    Configuration kubernetes = config.getSubConfig("dekorate") != null
        ? config.getSubConfig("dekorate").getSubConfig("kubernetes")
        : null;
    if (kubernetes != null && kubernetes.getString("image") != null) {
      container.withImage(kubernetes.getString("image"));
    }
  }
}
