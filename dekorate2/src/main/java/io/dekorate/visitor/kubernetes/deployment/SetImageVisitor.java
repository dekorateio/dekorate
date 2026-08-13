package io.dekorate.visitor.kubernetes.deployment;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class SetImageVisitor<C extends Configuration> extends TypedVisitor<ContainerBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.image";

  private Configuration config;

  public SetImageVisitor() {
  }

  public SetImageVisitor(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new SetImageVisitor<>(config);
  }

  @Override
  public void visit(ContainerBuilder container) {
    String image = config.resolveString(KEY_PATH);
    if (image != null) {
      container.withImage(image);
    }
  }
}
