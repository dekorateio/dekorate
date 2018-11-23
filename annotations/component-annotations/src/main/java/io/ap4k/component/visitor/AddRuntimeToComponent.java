package io.ap4k.component.visitor;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;

public class AddRuntimeToComponent extends TypedVisitor<ComponentSpecBuilder> {

  private final String runtime;

  public AddRuntimeToComponent(String runtime) {
    this.runtime = runtime;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component.withRuntime(runtime);
  }
}
