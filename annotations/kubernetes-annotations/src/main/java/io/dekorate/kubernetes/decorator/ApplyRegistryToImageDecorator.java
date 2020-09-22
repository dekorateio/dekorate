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

import io.dekorate.utils.Images;
import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

public class ApplyRegistryToImageDecorator extends Decorator<DeploymentBuilder> {

  private final String registry;
  private final String group;
  private final String name;
  private final String version;

  public ApplyRegistryToImageDecorator(String registry, String group, String name, String version) {
    this.registry = registry;
    this.group = group;
    this.name = name;
    this.version = version;
  }

  @Override
  public void visit(DeploymentBuilder deployment) {
    if (name.equals(deployment.getMetadata().getName())) {
      deployment.accept(new Decorator<ContainerFluent>() {
        @Override
        public void visit(ContainerFluent container) {
          if (container.getName().equals(name)) {
            String image = Images.getImage(registry, group, name, version);
            container.withImage(image);
          }
        }
      });
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ApplyImageDecorator.class };
  }
}
