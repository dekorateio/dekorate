/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.tekton.decorator;

import io.dekorate.kubernetes.decorator.Decorator;
import io.fabric8.tekton.pipeline.v1beta1.StepFluent;

public class AddEnvVarStepDecorator extends NamedStepDecorator<StepFluent<?>> {

  private final String envName;
  private final String envValue;

  public AddEnvVarStepDecorator(String taskName, String stepName, String envName, String envValue) {
    super(taskName, stepName);

    this.envName = envName;
    this.envValue = envValue;
  }

  @Override
  public void andThenVisit(StepFluent<?> step) {
    step.removeMatchingFromEnv(e -> envName.equals(e.getName()));
    step.addNewEnv()
        .withName(envName)
        .withValue(envValue)
        .endEnv();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { NamedTaskDecorator.class };
  }
}
