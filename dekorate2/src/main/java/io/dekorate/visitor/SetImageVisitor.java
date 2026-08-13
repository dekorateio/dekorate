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
    String image = config.resolveString("dekorate.kubernetes.image");
    if (image != null) {
      container.withImage(image);
    }
  }
}
