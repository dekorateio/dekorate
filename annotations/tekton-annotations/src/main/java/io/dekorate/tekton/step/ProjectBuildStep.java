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

public final class ProjectBuildStep extends Step {

  public static final String ID = "project-build";

  public static final String IMAGE_PARAM_NAME = "projectBuilderImage";
  public static final String IMAGE_PARAM_DESCRIPTION = "The image to use for performing project build";
  public static final String IMAGE_PARAM_REF = "$(inputs.params." + IMAGE_PARAM_NAME + ")";
  
  public static final String COMMAND_PARAM_NAME = "projectBuilderCommand";
  public static final String COMMAND_PARAM_DESCRIPTION = "The command to use for performing project build";
  public static final String COMMAND_PARAM_REF = "$(inputs.params." + COMMAND_PARAM_NAME + ")";

  public static final String ARGS_PARAM_NAME = "projectBuilderArgs";
  public static final String ARGS_PARAM_DESCRIPTION = "The command arguments to use for performing project build";
  public static final String ARGS_PARAM_REF = "$(inputs.params." + ARGS_PARAM_NAME + "[*])";
}
