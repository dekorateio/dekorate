package io.ap4k.component.visitor;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.decorator.Decorator;

public class AddRuntimeToComponent extends Decorator<ComponentSpecBuilder> {

  private final String runtime;

  public AddRuntimeToComponent(String runtime) {
    this.runtime = runtime;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component.withRuntime(runtime);
  }
}
