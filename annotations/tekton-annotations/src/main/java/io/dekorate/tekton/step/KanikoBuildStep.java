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

public class KanikoBuildStep extends ImageBuildStep<KanikoBuildStep> {

  public static final String DOCKERFILE_FORMAT = "--dockerfile=%s";
  public static final String DOCKERFILE_ARG = "--dockerfile=$(inputs.params." + PATH_TO_DOCKERFILE_PARAM_NAME + ")";
  public static final String CONTEXT_FORMAT = "--context=%s";
  public static final String CONTEXT_ARG = "--context=$(params." + PATH_TO_CONTEXT_PARAM_NAME + ")";
  public static final String IMAGE_DESTINATION_ARG = "--destination=$(resources.outputs.image.url)";

  public static final String BUILD_IMAGE_PARAM_DEFAULT = "gcr.io/kaniko-project/executor:v1.3.0";
  public static final String BUILD_COMMAND_PARAM_DEFAULT = "/kaniko/executor";

  public static final String PUSH_IMAGE_PARAM_DEFAULT = BUILD_COMMAND_PARAM_DEFAULT;
  public static final String PUSH_COMMAND_PARAM_DEFAULT = "/kaniko/executor";


  public KanikoBuildStep() {
    this(PATH_TO_CONTEXT_PARAM_DEFAULT, PATH_TO_DOCKERFILE_PARAM_DEFAULT, BUILD_IMAGE_PARAM_DEFAULT,
        BUILD_COMMAND_PARAM_DEFAULT, getDefaultBuildArguments(PATH_TO_DOCKERFILE_PARAM_DEFAULT, getContextPath()),
         null, null, null);
  }

  public KanikoBuildStep(String context, String dockerfile, String buildImage, String buildCommand,
      String[] buildArguments, String pushImage, String pushCommand, String[] pushArguments) {
    super(context, dockerfile, buildImage, buildCommand, buildArguments, pushImage, pushCommand, pushArguments);
  }

  @Override
  public KanikoBuildStep withContext(String context) {
    return new KanikoBuildStep(context, dockerfile,
                                buildImage, buildCommand, getDefaultBuildArguments(context, dockerfile),
                               null, null, null);
  }

  @Override
  public KanikoBuildStep withDockerfile(String dockerfile) {
    return new KanikoBuildStep(context, Strings.isNotNullOrEmpty(dockerfile) ? dockerfile : this.dockerfile,
                                buildImage, buildCommand, getDefaultBuildArguments(context, dockerfile),
                               null, null, null);
  }

  @Override
  public KanikoBuildStep withBuildImage(String buildImage) {
    return new KanikoBuildStep(context, dockerfile,
                               Strings.isNotNullOrEmpty(buildImage) ? buildImage : this.buildImage,
                               buildCommand, buildArguments, pushImage, pushCommand,
        pushArguments);
  }

  @Override
  public KanikoBuildStep withBuildCommand(String buildCommand) {
    return new KanikoBuildStep(context, dockerfile,
                               buildImage,
                               Strings.isNotNullOrEmpty(buildCommand) ? buildCommand : this.buildCommand,
                               buildArguments,
                               pushImage, pushCommand, pushArguments);
  }

  @Override
  public KanikoBuildStep withBuildArguments(String[] buildArguments) {
    return new KanikoBuildStep(context, dockerfile,
                               buildImage, buildCommand,
                               buildArguments != null && buildArguments.length > 0 ? buildArguments : this.buildArguments,
                               pushImage, pushCommand, pushArguments);
  }

  @Override
  public KanikoBuildStep withPushImage(String pushImage) {
    return new KanikoBuildStep(context, dockerfile,
                               buildImage, buildCommand, buildArguments,
                               Strings.isNotNullOrEmpty(pushImage) ? pushImage : this.pushImage,
                               pushCommand, pushArguments);
  }

  @Override
  public KanikoBuildStep withPushCommand(String pushCommand) {
    return new KanikoBuildStep(context, dockerfile,
                               buildImage, buildCommand, buildArguments,
                               pushImage,
                               Strings.isNotNullOrEmpty(pushCommand) ? pushCommand : this.pushCommand,
                               pushArguments);
  }

  @Override
  public KanikoBuildStep withPushArguments(String[] pushArguments) {
    return new KanikoBuildStep(context, dockerfile,
                               buildImage, buildCommand, buildArguments,
                               pushImage, pushCommand,
                               pushArguments != null && pushArguments.length > 0 ? pushArguments : this.pushArguments);
  }

  @Override
  public boolean isPushRequired() {
    return false;
  }
 
  private static String[] getDefaultBuildArguments(String context, String dockerfile) {
    return new String[] { String.format(DOCKERFILE_FORMAT, dockerfile), String.format(CONTEXT_FORMAT, context), IMAGE_DESTINATION_ARG };
  }
}
