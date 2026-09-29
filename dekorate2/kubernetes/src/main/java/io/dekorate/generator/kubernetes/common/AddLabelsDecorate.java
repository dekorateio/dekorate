package io.dekorate.generator.kubernetes.common;

import java.util.LinkedHashMap;
import java.util.Map;

import io.dekorate.core.Configuration;
import io.dekorate.core.VisitorFactory;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

public class AddLabelsDecorate<C extends Configuration> extends TypedVisitor<ObjectMetaBuilder>
    implements VisitorFactory {

  private static final String KEY_PATH = "dekorate.kubernetes.labels";

  private Configuration config;

  public AddLabelsDecorate() {
  }

  public AddLabelsDecorate(Configuration config) {
    this.config = config;
  }

  @Override
  public String getKeyPath() {
    return KEY_PATH;
  }

  @Override
  public TypedVisitor<?> create(Configuration config) {
    return new AddLabelsDecorate<>(config);
  }

  @Override
  public void visit(ObjectMetaBuilder meta) {
    Map<String, Object> labelsMap = config.resolveMap(KEY_PATH);
    if (labelsMap != null) {
      Map<String, String> labels = new LinkedHashMap<>();
      labelsMap.forEach((k, v) -> labels.put(k, v.toString()));
      meta.addToLabels(labels);
    }
  }
}
