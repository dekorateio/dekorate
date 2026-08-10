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

import static io.dekorate.tekton.step.DeployStep.PATH_TO_YML_PARAM_NAME;
import static io.dekorate.tekton.step.StepUtils.param;

import io.dekorate.kubernetes.decorator.Decorator;
import io.fabric8.tekton.pipeline.v1beta1.Step;
import io.fabric8.tekton.pipeline.v1beta1.StepBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddDeployStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String STEP_NAME = "deploy";
  private static final String DEPLOY_CMD = "kubectl";

  private final String stepName;
  private final String deployerImage;

  public AddDeployStepDecorator(String taskName, String deployerImage) {
    this(taskName, STEP_NAME, deployerImage);
  }

  public AddDeployStepDecorator(String taskName, String stepName, String deployerImage) {
    super(taskName);
    this.stepName = stepName;
    this.deployerImage = deployerImage;
  }

  @Override
  public void andThenVisit(TaskSpecFluent taskSpec) {
    taskSpec.addToSteps(createDeployStep());
  }

  public Step createDeployStep() {
    return new StepBuilder().withName(stepName).withImage(deployerImage).withCommand(DEPLOY_CMD)
        .withArgs(new String[] { "apply", "-f", param(PATH_TO_YML_PARAM_NAME) })
        .withWorkingDir(sourcePath())
        .build();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddProjectBuildStepDecorator.class, AddImageBuildStepDecorator.class };
  }
}
