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
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddKanikoImageBuildStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String STEP_NAME = "image-build";

  private static final String BUILDER_IMAGE_REF = "$(inputs.params.imageBuilderImage)";
  private static final String BUILDER_COMMAND_REF = "$(inputs.params.imageBuilderCommand)";
  private static final String BUILDER_ARGS_REF = "$(inputs.params.imageBuilderArgs[*])";

  private static final String KANIKO_CMD = "/kaniko/executor";
  private static final String DOCKERFILE_ARG = "--dockerfile=$(inputs.params.pathToDockerfile)";
  private static final String CONTEXT_ARG = "--context=$(params.pathToContext)";
  private static final String IMAGE_DESTINATION_ARG = "--destination=$(resources.outputs.image.url)";
  private static final String VERBOSITY_DEBUG = "--verbosity=debug";

  private static final String DOCKER_CONFIG = "DOCKER_CONFIG";
  private static final String DOCKER_CONFIG_DEFAULT = "/tekton/home/.docker";

  private final String stepName;
  private final String projectName;
  private final String image;
  private final String command;
  private final String[] args;


  public AddKanikoImageBuildStepDecorator(String taskName, String projectName) {
    this(taskName, STEP_NAME, projectName);
  }

  public AddKanikoImageBuildStepDecorator(String taskName, String stepName, String projectName) {
    this(taskName, stepName, projectName, BUILDER_IMAGE_REF, BUILDER_COMMAND_REF, BUILDER_ARGS_REF);
  }

  public AddKanikoImageBuildStepDecorator(String taskName, String stepName, String projectName, String image, String command, String... args) {
    super(taskName);
    this.stepName = stepName;
    this.projectName = projectName;
    this.image = Strings.isNotNullOrEmpty(image) ? image : BUILDER_IMAGE_REF;
    this.command = Strings.isNotNullOrEmpty(command) ? command : KANIKO_CMD;
    this.args = args != null && args.length != 0 ? args : new String[] {DOCKERFILE_ARG, CONTEXT_ARG, IMAGE_DESTINATION_ARG, VERBOSITY_DEBUG};
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
