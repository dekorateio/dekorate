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

import static io.dekorate.ConfigReference.generateConfigReferenceName;

import java.util.Arrays;
import java.util.List;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerFluent;

public class ApplyImageDecorator extends ApplicationContainerDecorator<ContainerFluent> implements WithConfigReferences {

  private final String image;

  public ApplyImageDecorator(String containerName, String image) {
    super(ANY, containerName);
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

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class,
        AddSidecarDecorator.class };
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    return Arrays.asList(buildConfigReferenceForImage());
  }

  private ConfigReference buildConfigReferenceForImage() {
    String property = generateConfigReferenceName("image", getContainerName(), getDeploymentName());
    String path = "spec.template.spec.containers.image";
    if (!Strings.equals(getDeploymentName(), ANY) && !Strings.equals(getContainerName(), ANY)) {
      path = "(metadata.name == " + getDeploymentName() + ")].spec.template.spec.containers"
          + ".(name == " + getContainerName() + ").image";
    } else if (!Strings.equals(getDeploymentName(), ANY)) {
      path = "(metadata.name == " + getDeploymentName() + ").spec.template.spec.containers.image";
    } else if (!Strings.equals(getContainerName(), ANY)) {
      path = "spec.template.spec.containers.(name == " + getContainerName() + ").image";
    }

    return new ConfigReference(property, path, image);
  }

}
