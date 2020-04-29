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

public class AddInitStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String INIT = "init";
  private static final String GIT = "git";
  private static final String BUSYBOX = "busybox";

  private final String stepName;
  private final String gitResourceName;
  private final String projectName;

  public AddInitStepDecorator(String taskName, String gitResourceName, String projectName) {
    this(taskName, INIT, gitResourceName, projectName);
  }

  public AddInitStepDecorator(String taskName, String stepName, String gitResourceName, String projectName) {
    super(taskName);
    this.stepName = stepName;
    this.gitResourceName = gitResourceName;
    this.projectName = projectName;
  }
   
  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addToSteps(createStep());
  }

  private Step createStep() {
    String sourcePath = sourcePath(projectName);
    return new StepBuilder()
      .withName(stepName)
      .withImage(BUSYBOX)
      .withCommand("cp").withArgs("-r", resourceInputPath(gitResourceName), sourcePath)
      .withWorkingDir(workspacePath(SOURCE))
      .build();
  }
}
