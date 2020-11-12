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

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.tekton.step.ImagePushStep;
import io.dekorate.utils.Strings;
import io.dekorate.deps.kubernetes.api.model.EnvVarBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddImagePushStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String PUSH_IMAGE_REF = "$(inputs.params.imagePushImage)";
  private static final String PUSH_COMMAND_REF = "$(inputs.params.imagePushCommand)";
  private static final String PUSH_ARGS_REF = "$(inputs.params.imagePushArgs[*])";

  private static final String DOCKER_CONFIG = "DOCKER_CONFIG";
  private static final String DOCKER_CONFIG_DEFAULT = "/tekton/home/.docker";

  private final String stepName;
  private final String projectName;
  private final String image;
  private final String command;
  private final String[] args;

  public AddImagePushStepDecorator(String taskName, String projectName) {
    this(taskName, ImagePushStep.ID, projectName);
  }

  public AddImagePushStepDecorator(String taskName, String stepName, String projectName) {
    this(taskName, stepName, projectName, PUSH_IMAGE_REF, PUSH_COMMAND_REF, PUSH_ARGS_REF);
  }

  public AddImagePushStepDecorator(String taskName, String stepName, String projectName, String image, String command, String... args) {
    super(taskName);
    this.stepName = stepName;
    this.projectName = projectName;
    this.image = Strings.isNotNullOrEmpty(image) ? image : PUSH_IMAGE_REF;
    this.command = Strings.isNotNullOrEmpty(command) ? command : PUSH_COMMAND_REF;
    this.args = args != null && args.length != 0 ? args : new String[] { PUSH_ARGS_REF };
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewStep()
      .withName(stepName)
      .withImage(image)
      .addToEnv(new EnvVarBuilder().withName(DOCKER_CONFIG).withValue(DOCKER_CONFIG_DEFAULT).build())
      .withCommand(command)
      .withArgs(args)
      .withWorkingDir(sourcePath(projectName))
      .endStep();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddInitStepDecorator.class, AddProjectBuildStepDecorator.class };
  }
}
