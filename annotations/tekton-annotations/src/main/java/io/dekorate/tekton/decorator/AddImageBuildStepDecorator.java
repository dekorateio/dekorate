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

import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;
import io.dekorate.kubernetes.decorator.Decorator;

public class AddImageBuildStepDecorator extends NamedTaskDecorator implements StepDecorator{


  private static final String BUILD_AND_PUSH = "build-and-push";
  private static final String BUILDER_IMAGE_REF = "$(inputs.params.builderImage)";
  private static final String KANIKO_CMD = "/kaniko/executor";
  private static final String DOCKERFILE_ARG = "--dockerfile=$(inputs.params.pathToDockerfile)";
  private static final String CONTEXT_ARG = "--context=$(params.pathToContext)";
  private static final String IMAGE_DESTINATION_ARG = "--destination=$(resources.outputs.image.url)";
  private static final String VERBOSITY_DEBUG = "--verbosity=debug";

  private static final String DOCKER_CONFIG = "DOCKER_CONFIG";
  private static final String DOCKER_CONFIG_DEFAULT = "/tekton/home/.docker";

  private final String stepName;
  private final String projectName;

  public AddImageBuildStepDecorator(String taskName, String projectName) {
    this(taskName, BUILD_AND_PUSH, projectName);
  }

  public AddImageBuildStepDecorator(String taskName, String stepName, String projectName) {
    super(taskName);
    this.stepName = stepName;
    this.projectName = projectName;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewStep()
          .withName(stepName)
          .withImage(BUILDER_IMAGE_REF)
          .addToEnv(new EnvVarBuilder().withName(DOCKER_CONFIG).withValue(DOCKER_CONFIG_DEFAULT).build())
          .withCommand(KANIKO_CMD)
          .addToArgs(DOCKERFILE_ARG)
          .addToArgs(CONTEXT_ARG)
          .addToArgs(IMAGE_DESTINATION_ARG)
          .addToArgs(VERBOSITY_DEBUG)
          .withWorkingDir(sourcePath(projectName))
      .endStep();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddInitStepDecorator.class, AddJavaBuildStepDecorator.class };
  }

}
