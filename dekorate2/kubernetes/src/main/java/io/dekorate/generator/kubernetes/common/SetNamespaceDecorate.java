package io.dekorate.generator.kubernetes.common;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

public class SetNamespaceDecorate<C extends Configuration> extends TypedVisitor<ObjectMetaBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.namespace";

  private Configuration config;

  public SetNamespaceDecorate() {
  }

  public SetNamespaceDecorate(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new SetNamespaceDecorate<>(config);
  }

  @Override
  public void visit(ObjectMetaBuilder meta) {
    String namespace = config.resolveString(KEY_PATH);
    if (namespace != null) {
      meta.withNamespace(namespace);
    }
  }
}
