package io.dekorate.generator.kubernetes.deployment;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

public class SetImageDecorate<C extends Configuration> extends TypedVisitor<ContainerBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.image";

  private Configuration config;

  public SetImageDecorate() {
  }

  public SetImageDecorate(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public String getGroup() {
    return "deployment";
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new SetImageDecorate<>(config);
  }

  @Override
  public void visit(ContainerBuilder container) {
    String image = config.resolveString(KEY_PATH);
    if (image != null) {
      container.withImage(image);
    }
  }
}
