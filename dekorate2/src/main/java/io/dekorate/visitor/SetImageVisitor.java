package io.dekorate.visitor;

import io.dekorate.core.Configuration;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class SetImageVisitor<C extends Configuration> extends TypedVisitor<ContainerBuilder> {

  private final String image;

  public SetImageVisitor(String image) {
    this.image = image;
  }

  @Override
  public void visit(ContainerBuilder container) {
    container.withImage(image);
  }
}
