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

public class KanikoBuildStep extends ImageBuildStep {

  public static final String PATH_TO_DOCKERFILE_PARAM_NAME = "pathToDockerfile";
  public static final String PATH_TO_DOCKERFILE_PARAM_DESCRIPTION = "Path to Dockerfile";

  public static final String DOCKERFILE_FORMAT = "--dockerfile=%s";
  public static final String DOCKERFILE_ARG = "--dockerfile=$(inputs.params." + PATH_TO_DOCKERFILE_PARAM_NAME+ ")";
  public static final String CONTEXT_FORMAT = "--context=%s";
  public static final String CONTEXT_ARG = "--context=$(params." + PATH_TO_CONTEXT_PARAM_NAME + ")";
  public static final String IMAGE_DESTINATION_ARG = "--destination=$(resources.outputs.image.url)";

  public static final String IMAGE_PARAM_DEFAULT = "gcr.io/kaniko-project/executor:v1.3.0";
  public static final String COMMAND_PARAM_DEFAULT = "/kaniko/executor";
  public static final String[] ARGS_PARAM_DEFAULT =  new String[] { DOCKERFILE_ARG, CONTEXT_ARG, IMAGE_DESTINATION_ARG };


  public static String[] getDefaultArguments(String dockerFile, String context) {
    return new String[] { String.format(DOCKERFILE_FORMAT, dockerFile), String.format(CONTEXT_FORMAT, context), IMAGE_DESTINATION_ARG};
  }
}
