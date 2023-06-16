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

import io.dekorate.tekton.step.ImageStep;

public class AddDockerSocketVolumeTaskDecorator extends AddHostPathVolumeTaskDecorator {

  public AddDockerSocketVolumeTaskDecorator(String taskName) {
    super(taskName, ImageStep.DOCKER_SOCKET_NAME, ImageStep.DOCKER_SOCKET_PATH, ImageStep.DOCKER_SOCKET_TYPE);
  }

  public AddDockerSocketVolumeTaskDecorator(String taskName, String name, String path, String type) {
    super(taskName, name, path, type);
  }
}
