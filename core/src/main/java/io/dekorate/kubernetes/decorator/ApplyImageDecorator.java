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
 */
package io.dekorate.kubernetes.decorator;

import io.dekorate.deps.kubernetes.api.model.ContainerFluent;

public class ApplyImageDecorator extends ApplicationContainerDecorator<ContainerFluent> {

  private final String image;

  public ApplyImageDecorator(String containerName, String image) {
    super(null, containerName);
    this.image = image;
  }

  public ApplyImageDecorator(String deploymentName, String containerName, String image) {
    super(deploymentName, containerName);
    this.image = image;
  }

  @Override
  public void andThenVisit(ContainerFluent container) {
      container.withImage(image);
  }
}
