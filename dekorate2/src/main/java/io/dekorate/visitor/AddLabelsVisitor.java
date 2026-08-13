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
  @SuppressWarnings("unchecked")
  public void visit(ObjectMetaBuilder meta) {
    Configuration kubernetes = config.getSubConfig("dekorate") != null
        ? config.getSubConfig("dekorate").getSubConfig("kubernetes")
        : null;
    if (kubernetes == null) {
      return;
    }

    Map<String, Object> labelsMap = kubernetes.getMap("labels");
    if (labelsMap != null) {
      Map<String, String> labels = new LinkedHashMap<>();
      labelsMap.forEach((k, v) -> labels.put(k, v.toString()));
      meta.addToLabels(labels);
    }
  }
}
