package io.dekorate.generator.kubernetes.common;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

public class SetNameDecorate<C extends Configuration> extends TypedVisitor<ObjectMetaBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.name";

  private Configuration config;

  public SetNameDecorate() {
  }

  public SetNameDecorate(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new SetNameDecorate<>(config);
  }

  @Override
  public void visit(ObjectMetaBuilder meta) {
    String name = config.resolveString(KEY_PATH);
    if (name != null) {
      meta.withName(name);
    }
  }
}
