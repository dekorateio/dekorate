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

public abstract class ImageBuildStep<T extends ImageBuildStep> implements Step {

  public static final String ID = "image-build";

  public static final String PATH_TO_DOCKERFILE_PARAM_NAME = "pathToDockerfile";
  public static final String PATH_TO_DOCKERFILE_PARAM_DESCRIPTION = "Path to Dockerfile";
  public static final String PATH_TO_DOCKERFILE_PARAM_DEFAULT = "Dockerfile";

  public static final String IMAGE_PARAM_NAME = "imageBuilderImage";
  public static final String IMAGE_PARAM_DESCRIPTION = "The image to use for performing project build";
  public static final String IMAGE_PARAM_REF = "$(inputs.params." + IMAGE_PARAM_NAME + ")";

  public static final String COMMAND_PARAM_NAME = "imageBuilderCommand";
  public static final String COMMAND_PARAM_DESCRIPTION = "The command to use for performing project build";
  public static final String COMMAND_PARAM_REF = "$(inputs.params." + COMMAND_PARAM_NAME + ")";

  public static final String ARGS_PARAM_NAME = "imageBuilderArgs";
  public static final String ARGS_PARAM_DESCRIPTION = "The command arguments to use for performing project build";
  public static final String ARGS_PARAM_REF = "$(inputs.params." + ARGS_PARAM_NAME + "[*])";

  protected final String context;
  protected final String dockerfile;

  protected final String buildImage;
  protected final String buildCommand;
  protected final String[] buildArguments;
  
  protected final String pushImage;
  protected final String pushCommand;
  protected final String[] pushArguments;

  public ImageBuildStep(String context, String dockerfile, String buildImage, String buildCommand, String[] buildArguments, String pushImage, String pushCommand, String[] pushArguments) {
    this.context = context;
    this.dockerfile = dockerfile;
    this.buildImage = buildImage;
    this.buildCommand = buildCommand;
    this.buildArguments = buildArguments;
    this.pushImage = pushImage;
    this.pushCommand = pushCommand;
    this.pushArguments = pushArguments;
  }

  /**
   * Create a new step using the specified context.
   * @param context the specified context.
   * @return the updated step.
   */
  public abstract T withContext(String context);

  /**
   * Create a new step using the specified dockerfile.
   * @param dockerfile the specified dockerfile.
   * @return the updated step.
   */
  public abstract T withDockerfile(String dockerfile);

  /**
   * Create a new step using the specified build image.
   * @param buildImage the specified build image.
   * @return the updated step
   */
  public abstract T withBuildImage(String buildImage);

  /**
   * Create a new step using the specified build command.
   * @param buildCommand the specified build command.
   * @return the updated step.
   */
  public abstract T withBuildCommand(String buildCommand);

  /**
   * Create a new step using the specified build arguments.
   * @param buildArguments the specified build arguments.
   * @return the updated step.
   */
  public abstract T withBuildArguments(String[] buildArugments);

  /**
   * Create a new step using the specified push image.
   * @param pushImage the specified push image.
   * @return the updated step.
   */
  public abstract T withPushImage(String pushImage);

  /**
   * Create a new step using the specified push command.
   * @param pushCommand the specified push command.
   * @return the updated step.
   */
  public abstract T withPushCommand(String pushCommand);

  /**
   * Create a new step using the specified push arguments.
   * @param pushArguments the specified push arguments.
   * @return the updated step.
   */
  public abstract T withPushArguments(String[] pushArguments);


  /**
   * Returns true if the current build step requires an explicit push.
   * 
   * @return true, if explict push is required.
   */
  public boolean isPushRequired() {
    return false;
  }

  /**
   * Returns true if the current build step requires a docker socket
   * 
   * @return true, if a docker socket is required.
   */
  public boolean isDockerSocketRequired() {
    return false;
  }

  /**
   * The conntext.
   * @return the context.
   */
  public String getContext() {
    return this.context;
  }

  /**
   * The dockerfile.
   * @return the dockerfile.
   */
  public String getDockerfile() {
    return this.dockerfile;
  }

  /**
   * The build command.
   * 
   * @return the build command.
   */
  public String getBuildImage() {
    return buildImage;
  }

  /**
   * The build command.
   * 
   * @return the build command.
   */
  public String getBuildCommand() {
    return buildCommand;
  }

  /**
   * The build arguments.
   * 
   * @return the build arguments.
   */
  public String[] getBuildArguments() {
    return buildArguments;
  }

  /**
   * The push image.
   * 
   * @return the push image.
   */
  public String getPushImage() {
    return pushImage;
  }

  /**
   * The push command.
   * 
   * @return the push command.
   */
  public String getPushCommand() {
    return pushCommand;
  }

  /**
   * The push arguments.
   * 
   * @return the push arguments.
   */
  public String[] getPushArguments() {
    return pushArguments;
  }
}
