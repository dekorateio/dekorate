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

import io.dekorate.deps.tekton.pipeline.v1beta1.Step;
import io.dekorate.deps.tekton.pipeline.v1beta1.StepBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;
import io.dekorate.kubernetes.decorator.Decorator;

public class AddDeployStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String STEP_NAME = "deploy";
  private static final String DEPLOY_CMD = "kubectl";
  private static final String PATH_TO_YML_PARAM_NAME = "pathToYml";

  private final String stepName;
  private final String projectName;
  private final String deployerImage;

  public AddDeployStepDecorator(String taskName, String stepName, String projectName, String deployerImage) {
    super(taskName);
    this.stepName = stepName;
    this.projectName = projectName;
    this.deployerImage = deployerImage;
  }

  public AddDeployStepDecorator(String taskName, String projectName, String deployerImage) {
    this(taskName, STEP_NAME, projectName, deployerImage);
  }

  @Override
  public void andThenVisit(TaskSpecFluent taskSpec) {
    taskSpec.addToSteps(createDeployStep());
  }

  public Step createDeployStep() {
    return new StepBuilder().withName(stepName).withImage(deployerImage).withCommand(DEPLOY_CMD)
        .withArgs(new String[] { "apply", "-f", param(PATH_TO_YML_PARAM_NAME) }).withWorkingDir(sourcePath(projectName))
        .build();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddInitStepDecorator.class, AddProjectBuildStepDecorator.class, AddImageBuildStepDecorator.class };
  }
}
