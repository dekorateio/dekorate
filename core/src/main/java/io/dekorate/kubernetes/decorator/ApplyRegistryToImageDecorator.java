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
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerFluent;

public class ApplyRegistryToImageDecorator extends ApplicationContainerDecorator<ContainerFluent<?>> {

  private final String registry;
  private final boolean replaceExisting;

  public ApplyRegistryToImageDecorator(String resourceName, String containerName, String registry, boolean replaceExisitng) {
    super(resourceName, containerName);
    this.registry = registry;
    this.replaceExisting = replaceExisitng;
  }

  public ApplyRegistryToImageDecorator(String resourceName, String containerName, String registry) {
    this(resourceName, containerName, registry, true);
  }

  public ApplyRegistryToImageDecorator(String containerName, String registry) {
    this(ANY, containerName, registry);
  }

  public ApplyRegistryToImageDecorator(String registry) {
    this(ANY, ANY, registry);
  }

  @Override
  public void andThenVisit(ContainerFluent<?> container) {
    String image = container.getImage();
    String existingRegistry = Images.getRegistry(image);
    String repository = Images.getRepository(image);
    String tag = Images.getTag(image);

    if (replaceExisting || Strings.isNullOrEmpty(existingRegistry)) {
      container.withImage(Images.getImage(registry, repository, tag));
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ApplyImageDecorator.class, AddInitContainerDecorator.class };
  }
}
