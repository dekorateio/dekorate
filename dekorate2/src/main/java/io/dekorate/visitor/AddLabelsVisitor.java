package io.dekorate.visitor;

import java.util.LinkedHashMap;
import java.util.Map;

import io.dekorate.core.Configuration;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

public class AddLabelsVisitor<C extends Configuration> extends TypedVisitor<ObjectMetaBuilder> {

  private final Configuration config;

  public AddLabelsVisitor(Configuration config) {
    this.config = config;
  }

  @Override
  public void visit(ObjectMetaBuilder meta) {
    Map<String, Object> labelsMap = config.resolveMap("dekorate.kubernetes.labels");
    if (labelsMap != null) {
      Map<String, String> labels = new LinkedHashMap<>();
      labelsMap.forEach((k, v) -> labels.put(k, v.toString()));
      meta.addToLabels(labels);
    }
  }
}
