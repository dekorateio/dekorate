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

import io.dekorate.deps.kubernetes.api.model.EnvVarBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.ArrayOrString;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddJibMavenBuildStepDecorator extends NamedTaskDecorator implements StepDecorator {

  private static final String BUILD_AND_PUSH = "build-and-push";
  private static final String BUILDER_IMAGE_REF = "$(inputs.params.BUILDER_IMAGE)";
  private static final String CMD = "mvn";
  private static final String[] ARGS = {"-B", "compile", "com.google.cloud.tools:jib-maven-plugin:build", "-Djib.to.image=$(resources.outputs.image.url)", "-Djib.allowInsecureRegistries=$(params.INSECUREREGISTRY)", "-Djib.httpTimeout=60000"};

  private static final String DOCKER_CONFIG = "DOCKER_CONFIG";
  private static final String DOCKER_CONFIG_DEFAULT = "/tekton/home/.docker";

  private static final String INSECUREREGISTRY = "INSECUREREGISTRY";
  private static final String INSECUREREGISTRY_DESCRIPTION = "Whether to allow insecure registry";
  private static final String INSECUREREGISTRY_DEFAULT = "true";

  private static final String HTTP_TIMEOUT = "HTTP_TIMEOUT";
  private static final String HTTP_TIMEOUT_DESCRIPTION = "The http timeout";
  private static final String HTTP_TIMEOUT_DEFAULT = "60000";


  private final String stepName;

  public AddJibMavenBuildStepDecorator(String taskName) {
    this(taskName, BUILD_AND_PUSH);
  }

  public AddJibMavenBuildStepDecorator(String taskName, String stepName) {
    super(taskName);
    this.stepName = stepName;
  }


  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {

    taskSpec
      .addNewParam()
        .withName(INSECUREREGISTRY)
        .withDescription(INSECUREREGISTRY_DESCRIPTION)
        .withNewDefault().withStringVal(INSECUREREGISTRY_DEFAULT).endDefault()
      .endParam()
      .addNewParam()
        .withName(HTTP_TIMEOUT)
        .withType("string")
        .withDescription(HTTP_TIMEOUT_DESCRIPTION)
        .withDefault(new ArrayOrString(HTTP_TIMEOUT_DEFAULT))
      .endParam()
      .addNewStep()
          .withName(stepName)
          .withImage(BUILDER_IMAGE_REF)
          .addToEnv(new EnvVarBuilder().withName(DOCKER_CONFIG).withValue(DOCKER_CONFIG_DEFAULT).build())
          .withCommand(CMD)
          .withArgs(ARGS)
          .withWorkingDir(WORKING_DIR)
      .endStep();
  }

}
