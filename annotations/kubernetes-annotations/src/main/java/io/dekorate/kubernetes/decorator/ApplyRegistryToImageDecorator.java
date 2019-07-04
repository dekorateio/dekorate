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
import io.dekorate.deps.kubernetes.api.model.apps.DeploymentBuilder;
import io.dekorate.kubernetes.decorator.ApplyImageDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.utils.Images;
import io.dekorate.Resources;

public class ApplyRegistryToImageDecorator extends Decorator<DeploymentBuilder> {

  private final String registry;
  private final Resources resources;

  public ApplyRegistryToImageDecorator(Resources resources, String registry) {
    this.registry=registry;
    this.resources=resources;
  }

  @Override
  public void visit(DeploymentBuilder deployment) {
    String name = resources.getName();
    if (name.equals(deployment.getMetadata().getName())) {
      deployment.accept(new Decorator<ContainerFluent>() {
        @Override
        public void visit(ContainerFluent container) {
          if (container.getName().equals(name)) {
            String image = Images.getImage(registry, resources.getGroup(), resources.getName(), resources.getVersion());
            container.withImage(image);
          }
        }
      });
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[]{ApplyImageDecorator.class};
    }
}
