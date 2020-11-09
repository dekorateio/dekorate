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
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.tekton.step.ProjectBuildStep;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;


public class AddProjectBuildStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private final String projectName;
  private final String stepName;
  private final String image;
  private final String command;
  private final String[] arguments;

  public AddProjectBuildStepDecorator(String taskName, String projectName, BuildImage builder) {
    this(taskName, ProjectBuildStep.ID, projectName, builder);
  }

  public AddProjectBuildStepDecorator(String taskName, String stepName, String projectName) {
    this(taskName, stepName, projectName, ProjectBuildStep.IMAGE_PARAM_REF, ProjectBuildStep.COMMAND_PARAM_REF, ProjectBuildStep.ARGS_PARAM_REF);
  }

  public AddProjectBuildStepDecorator(String taskName, String stepName, String projectName, BuildImage builder) {
    this(taskName, stepName, projectName, builder.getImage(), builder.getCommand(), builder.getArguments());
  }
  
  public AddProjectBuildStepDecorator(String taskName, String stepName, String projectName, String image, String command, String... arguments) {
    super(taskName);
    this.stepName = stepName;
    this.projectName = projectName;
    this.image = image;
    this.command = command;
    this.arguments = arguments;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewStep()
        .withName(stepName)
        .withImage(image)
        .withCommand(command)
        .withArgs(arguments)
        .withWorkingDir(sourcePath(projectName))
        .endStep();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddInitStepDecorator.class };
  }
}
