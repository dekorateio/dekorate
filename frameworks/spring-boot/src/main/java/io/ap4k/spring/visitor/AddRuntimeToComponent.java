package io.ap4k.spring.visitor;

import io.ap4k.component.model.ComponentSpecBuilder;

public class AddRuntimeToComponent extends TypedVisitor<ComponentSpecBuilder> {

  // TODO - Should I decorate Component here ?
  @Override
  public void visit(ComponentSpecBuilder component) {
    component.withRuntime(RuntimeFrameworkDetector.detect());
  }
}
