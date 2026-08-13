package io.dekorate.visitor;

import java.util.Map;

import io.dekorate.core.Configuration;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

public class AddLabelsVisitor<C extends Configuration> extends TypedVisitor<ObjectMetaBuilder> {

  private final Map<String, String> labels;

  public AddLabelsVisitor(Map<String, String> labels) {
    this.labels = labels;
  }

  @Override
  public void visit(ObjectMetaBuilder meta) {
    meta.addToLabels(labels);
  }
}
