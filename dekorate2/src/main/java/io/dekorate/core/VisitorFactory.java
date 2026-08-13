package io.dekorate.core;

import io.fabric8.kubernetes.api.builder.TypedVisitor;

public interface VisitorFactory {

  String getKeyPath();

  TypedVisitor<?> create(Configuration config);
}
