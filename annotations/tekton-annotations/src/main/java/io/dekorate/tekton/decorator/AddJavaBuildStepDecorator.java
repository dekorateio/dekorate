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
 *
 **/

package io.dekorate.tekton.decorator;

import io.dekorate.BuildImage;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;
import io.dekorate.kubernetes.decorator.Decorator;

public class AddJavaBuildStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String JAVA = "java";
  private static final String DASH = "-";
  private static final String BUILD = "build";

  private final String projectName;
  private final String stepName;
  private final BuildImage builder;

  public AddJavaBuildStepDecorator(String taskName,  String projectName, BuildImage builder) {
    this(taskName, JAVA  + DASH + BUILD, projectName, builder);
  }

  public AddJavaBuildStepDecorator(String taskName, String stepName, String projectName, BuildImage builder) {
    super(taskName);
    this.stepName = stepName;
    this.projectName = projectName;
    this.builder = builder;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewStep()
      .withName(stepName)
      .withImage(builder.getImage())
      .withCommand(builder.getCommand())
      .withArgs(builder.getArguments())
      .withWorkingDir(sourcePath(projectName))
      .endStep();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddInitStepDecorator.class };
  }
}
