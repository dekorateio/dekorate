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
package io.ap4k.docker.decorator;

import io.ap4k.deps.kubernetes.api.model.ContainerFluent;
import io.ap4k.deps.openshift.api.model.DeploymentConfigBuilder;
import io.ap4k.kubernetes.decorator.Decorator;
import io.ap4k.utils.Images;


public class ApplyRegistryToImageDecorator extends Decorator<DeploymentConfigBuilder> {

  private final String name;
  private final String image;

  public ApplyRegistryToImageDecorator(String registry, String group, String name, String version) {
    this.name = name;
    this.image = Images.getImage(registry, group, name, version);
  }

  @Override
  public void visit(DeploymentConfigBuilder deploymentConfig) {
    if (name.equals(deploymentConfig.getMetadata().getName())) {
      deploymentConfig.accept(new Decorator<ContainerFluent>() {
        @Override
        public void visit(ContainerFluent container) {
          if (container.getName().equals(name)) {
            container.withImage(image);
          }
        }
      });
    }
  }
}
