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
import io.dekorate.kubernetes.annotation.ImagePullPolicy;

public class ApplyImagePullPolicyDecorator extends Decorator<ContainerFluent>  {

  private final ImagePullPolicy imagePullPolicy;

  public ApplyImagePullPolicyDecorator(ImagePullPolicy imagePullPolicy) {
    this.imagePullPolicy = imagePullPolicy != null ? imagePullPolicy : ImagePullPolicy.IfNotPresent;
  }

  @Override
  public void visit(ContainerFluent container) {
    container.withImagePullPolicy(imagePullPolicy.name());
  }

  public Class<? extends Decorator>[] after() {
    return new Class[]{ResourceProvidingDecorator.class, ContainerDecorator.class, AddSidecarDecorator.class};
  }

}
