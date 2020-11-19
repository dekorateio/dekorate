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

package io.dekorate.tekton.step;

import static io.dekorate.tekton.util.TektonUtils.getContextPath;
import io.dekorate.utils.Strings;

public class DockerBuildStep extends ImageBuildStep<DockerBuildStep> {

  public static final String BUILD = "build";
  public static final String PUSH = "push";
  public static final String TARGET = "--target";
  public static final String FILE = "--file";
  public static final String DOT = ".";

  public static final String DOCKERFILE_ARG = "$(inputs.params." + PATH_TO_DOCKERFILE_PARAM_NAME+ ")";
  public static final String CONTEXT_ARG = "$(inputs.params." + PATH_TO_CONTEXT_PARAM_NAME + ")";
  public static final String IMAGE_TARGET_ARG = "$(resources.outputs.image.url)";

  public static final String BUILD_IMAGE_PARAM_DEFAULT = "docker.io/docker:19.03.13";
  public static final String BUILD_COMMAND_PARAM_DEFAULT = "docker";

  public static final String PUSH_IMAGE_PARAM_DEFAULT = BUILD_COMMAND_PARAM_DEFAULT;
  public static final String PUSH_COMMAND_PARAM_DEFAULT = "docker";

  public DockerBuildStep() {
    this(PATH_TO_CONTEXT_PARAM_DEFAULT, PATH_TO_DOCKERFILE_PARAM_DEFAULT,
         BUILD_IMAGE_PARAM_DEFAULT, BUILD_COMMAND_PARAM_DEFAULT, getDefaultBuildArguments(PATH_TO_DOCKERFILE_PARAM_DEFAULT, getContextPath()),
         PUSH_IMAGE_PARAM_DEFAULT, PUSH_COMMAND_PARAM_DEFAULT, getDefaultPushArguments(PATH_TO_DOCKERFILE_PARAM_DEFAULT, PATH_TO_CONTEXT_PARAM_DEFAULT));
  }

  public DockerBuildStep(String context, String dockerfile, String buildImage, String buildCommand,
      String[] buildArguments, String pushImage, String pushCommand, String[] pushArguments) {
    super(context, dockerfile, buildImage, buildCommand, buildArguments, pushImage, pushCommand, pushArguments);
  }

  @Override
  public DockerBuildStep withContext(String context) {
    return new DockerBuildStep(context, dockerfile,
                                buildImage, buildCommand, getDefaultBuildArguments(context, dockerfile),
                                pushImage, pushCommand, getDefaultPushArguments(context, dockerfile));
  }

  @Override
  public DockerBuildStep withDockerfile(String dockerfile) {
    return new DockerBuildStep(context, Strings.isNotNullOrEmpty(dockerfile) ? dockerfile : this.dockerfile,
                                buildImage, buildCommand, getDefaultBuildArguments(context, dockerfile),
                                pushImage, pushCommand, getDefaultPushArguments(context, dockerfile));
  }

  @Override
  public DockerBuildStep withBuildImage(String buildImage) {
    return new DockerBuildStep(context, dockerfile,
                               Strings.isNotNullOrEmpty(buildImage) ? buildImage : this.buildImage,
                               buildCommand, buildArguments, pushImage, pushCommand,
        pushArguments);
  }

  @Override
  public DockerBuildStep withBuildCommand(String buildCommand) {
    return new DockerBuildStep(context, dockerfile,
                               buildImage,
                               Strings.isNotNullOrEmpty(buildCommand) ? buildCommand : this.buildCommand,
                               buildArguments,
                               pushImage, pushCommand, pushArguments);
  }

  @Override
  public DockerBuildStep withBuildArguments(String[] buildArguments) {
    return new DockerBuildStep(context, dockerfile,
                               buildImage, buildCommand,
                               buildArguments != null && buildArguments.length > 0 ? buildArguments : this.buildArguments,
                               pushImage, pushCommand, pushArguments);
  }

  @Override
  public DockerBuildStep withPushImage(String pushImage) {
    return new DockerBuildStep(context, dockerfile,
                               buildImage, buildCommand, buildArguments,
                               Strings.isNotNullOrEmpty(pushImage) ? pushImage : this.pushImage,
                               pushCommand, pushArguments);
  }

  @Override
  public DockerBuildStep withPushCommand(String pushCommand) {
    return new DockerBuildStep(context, dockerfile,
                               buildImage, buildCommand, buildArguments,
                               pushImage,
                               Strings.isNotNullOrEmpty(pushCommand) ? pushCommand : this.pushCommand,
                               pushArguments);
  }

  @Override
  public DockerBuildStep withPushArguments(String[] pushArguments) {
    return new DockerBuildStep(context, dockerfile,
                               buildImage, buildCommand, buildArguments,
                               pushImage, pushCommand,
                               pushArguments != null && pushArguments.length > 0 ? pushArguments : this.pushArguments);
  }

  @Override
  public boolean isPushRequired() {
    return true;
  }

 @Override
 public boolean isDockerSocketRequired() {
     return true;
 } 

  private static String[] getDefaultBuildArguments(String context, String dockerfile) {
    return new String[] { BUILD, TARGET, IMAGE_TARGET_ARG, FILE, dockerfile, context};
  }

  private static String[] getDefaultPushArguments(String context, String dockerfile) {
    return new String[] { PUSH, IMAGE_TARGET_ARG };
  }


}
