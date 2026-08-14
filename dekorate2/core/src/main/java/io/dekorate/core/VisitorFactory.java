package io.dekorate.core;

import io.fabric8.kubernetes.api.builder.TypedVisitor;

public interface VisitorFactory {

  String getKeyPath();

  default String getGroup() {
    return "common";
  }

  TypedVisitor<?> create(Configuration config);
}
