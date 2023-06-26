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

import static io.dekorate.tekton.step.ImageBuildStep.ARGS_PARAM_REF;
import static io.dekorate.tekton.step.ImageBuildStep.COMMAND_PARAM_REF;
import static io.dekorate.tekton.step.ImageBuildStep.IMAGE_PARAM_REF;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.tekton.step.ImageBuildStep;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddImageBuildStepDecorator extends NamedTaskDecorator implements StepDecorator {

  public static final String DOCKER_CONFIG = "DOCKER_CONFIG";
  private static final String DOCKER_CONFIG_DEFAULT = "/tekton/home/.docker";

  private final String stepName;
  private final String image;
  private final String command;
  private final String[] args;

  public AddImageBuildStepDecorator(String taskName) {
    this(taskName, ImageBuildStep.ID);
  }

  public AddImageBuildStepDecorator(String taskName, String stepName) {
    this(taskName, stepName, IMAGE_PARAM_REF, COMMAND_PARAM_REF, ARGS_PARAM_REF);
  }

  public AddImageBuildStepDecorator(String taskName, String stepName, String image, String command, String... args) {
    super(taskName);
    this.stepName = stepName;
    this.image = Strings.isNotNullOrEmpty(image) ? image : IMAGE_PARAM_REF;
    this.command = Strings.isNotNullOrEmpty(command) ? command : COMMAND_PARAM_REF;
    this.args = args != null && args.length != 0 ? args : new String[] { ARGS_PARAM_REF };
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewStep()
        .withName(stepName)
        .withImage(image)
        .addToEnv(new EnvVarBuilder().withName(DOCKER_CONFIG).withValue(DOCKER_CONFIG_DEFAULT).build())
        .withCommand(command)
        .withArgs(args)
        .withWorkingDir(sourcePath())
        .endStep();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddProjectBuildStepDecorator.class };
  }
}
