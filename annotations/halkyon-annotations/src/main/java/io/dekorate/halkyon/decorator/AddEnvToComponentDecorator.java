/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.halkyon.decorator;

import io.dekorate.doc.Description;
import io.dekorate.halkyon.model.ComponentSpecBuilder;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.decorator.Decorator;

@Description("Add environment variable to component.")
public class AddEnvToComponentDecorator extends Decorator<ComponentSpecBuilder> {

  private final Env env;

  public AddEnvToComponentDecorator(Env env) {
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
