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

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.deps.tekton.pipeline.v1beta1.StepFluent;

@Description("A decorator that applies the command args to the task step.")
public class ApplyArgsDecorator extends TektonStepDecorator<StepFluent> {

  private final String[] argument;

  public ApplyArgsDecorator(String containerName, String... argument) {
    super(null, containerName);
    this.argument = argument;
  }
  public ApplyArgsDecorator(String deploymentName, String containerName, String... argument) {
    super(deploymentName, containerName);
    this.argument = argument;
  }

  @Override
  public void andThenVisit(StepFluent step) {
    if (isApplicable(step) && argument != null && argument.length > 0) {
      step.withArgs(argument);
    }
  }

  public Class<? extends Decorator>[] after() {
    return new Class[]{ResourceProvidingDecorator.class};
  }
}
