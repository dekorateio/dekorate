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

import static io.dekorate.tekton.step.StepUtils.param;

public class ImagePushStep implements Step {

  public static final String ID = "image-push";

  public static final String IMAGE_PARAM_NAME = "imagePushImage";
  public static final String IMAGE_PARAM_DESCRIPTION = "The image to use for performing project push";
  public static final String IMAGE_PARAM_REF = param(IMAGE_PARAM_NAME);

  public static final String COMMAND_PARAM_NAME = "imagePushCommand";
  public static final String COMMAND_PARAM_DESCRIPTION = "The command to use for performing project push";
  public static final String COMMAND_PARAM_REF = param(COMMAND_PARAM_NAME);

  public static final String ARGS_PARAM_NAME = "imagePushArgs";
  public static final String ARGS_PARAM_DESCRIPTION = "The command arguments to use for performing project push";
  public static final String ARGS_PARAM_REF = param(ARGS_PARAM_NAME);

}
