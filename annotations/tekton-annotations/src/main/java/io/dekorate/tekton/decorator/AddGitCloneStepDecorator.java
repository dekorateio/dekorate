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
import io.dekorate.tekton.step.GitCloneStep;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddGitCloneStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String GIT_CLONE_SCRIPT = "/git-clone-script.sh";
  private static final String URL = "PARAM_URL";
  private static final String REVISION = "PARAM_REVISION";
  private static final String SUBDIRECTORY = "PARAM_SUBDIRECTORY";

  private final String stepName;

  public AddGitCloneStepDecorator(String taskName, String stepName) {
    super(taskName);
    this.stepName = stepName;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    taskSpec.addNewStep()
        .withImage(GitCloneStep.IMAGE_PARAM_REF)
        .withName(stepName)
        .withScript(Strings.read(AddGitCloneStepDecorator.class.getResourceAsStream(GIT_CLONE_SCRIPT)))
        .addToEnv(new EnvVarBuilder().withName(URL).withValue(GitCloneStep.REPO_URL_PARAM_REF).build())
        .addToEnv(new EnvVarBuilder().withName(REVISION).withValue(GitCloneStep.REVISION_PARAM_REF).build())
        .withWorkingDir(sourcePath())
        .endStep();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { AddInitStepDecorator.class };
  }
}
