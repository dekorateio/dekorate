package io.dekorate.generator.kubernetes.common;

import java.util.LinkedHashMap;
import java.util.Map;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

public class AddAnnotationsDecorate<C extends Configuration> extends TypedVisitor<ObjectMetaBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.annotations";

  private Configuration config;

  public AddAnnotationsDecorate() {
  }

  public AddAnnotationsDecorate(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new AddAnnotationsDecorate<>(config);
  }

  @Override
  public void visit(ObjectMetaBuilder meta) {
    Map<String, Object> annotationsMap = config.resolveMap(KEY_PATH);
    if (annotationsMap != null) {
      Map<String, String> annotations = new LinkedHashMap<>();
      annotationsMap.forEach((k, v) -> annotations.put(k, v.toString()));
      meta.addToAnnotations(annotations);
    }
  }
}
