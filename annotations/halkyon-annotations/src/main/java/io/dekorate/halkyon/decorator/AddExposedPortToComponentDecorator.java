package io.dekorate.halkyon.decorator;

import io.dekorate.halkyon.model.ComponentSpecBuilder;
import io.dekorate.kubernetes.decorator.Decorator;

public class AddExposedPortToComponentDecorator extends Decorator<ComponentSpecBuilder> {
  
  private final int port;
  
  public AddExposedPortToComponentDecorator(int port) {
    this.port = port;
  }
  
  @Override
  public void visit(ComponentSpecBuilder componentSpecBuilder) {
    componentSpecBuilder.withPort(port);
  }
}
