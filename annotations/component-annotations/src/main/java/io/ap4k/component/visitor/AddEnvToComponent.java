package io.ap4k.component.visitor;

import io.ap4k.component.model.ComponentSpecBuilder;
import io.ap4k.decorator.Decorator;
import io.ap4k.config.Env;

public class AddEnvToComponent extends Decorator<ComponentSpecBuilder> {

  private final Env env;

  public AddEnvToComponent (Env env) {
    this.env = env;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {
    component.addNewEnv()
      .withName(env.getName())
      .withValue(env.getValue())
      .endEnv();
  }
}
