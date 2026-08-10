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

import io.dekorate.kubernetes.annotation.ImagePullPolicy;
import io.fabric8.kubernetes.api.model.ContainerFluent;

public class ApplyImagePullPolicyDecorator extends ApplicationContainerDecorator<ContainerFluent<?>> {

  private final ImagePullPolicy imagePullPolicy;

  public ApplyImagePullPolicyDecorator(String deploymentName, String containerName, ImagePullPolicy imagePullPolicy) {
    super(deploymentName, containerName);
    this.imagePullPolicy = imagePullPolicy;
  }

  public ApplyImagePullPolicyDecorator(String containerName, ImagePullPolicy imagePullPolicy) {
    this(ANY, containerName, imagePullPolicy);
  }

  public ApplyImagePullPolicyDecorator(ImagePullPolicy imagePullPolicy) {
    this(ANY, imagePullPolicy);
  }

  public ApplyImagePullPolicyDecorator(String deploymentName, String containerName, String imagePullPolicy) {
    this(deploymentName, containerName, ImagePullPolicy.valueOf(imagePullPolicy));
  }

  public ApplyImagePullPolicyDecorator(String containerName, String imagePullPolicy) {
    this(ANY, containerName, imagePullPolicy);
  }

  public ApplyImagePullPolicyDecorator(String imagePullPolicy) {
    this(ANY, imagePullPolicy);
  }

  @Override
  public void andThenVisit(ContainerFluent<?> container) {
    container.withImagePullPolicy(imagePullPolicy != null ? imagePullPolicy.name() : "IfNotPresent");
  }
}
