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

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.adapter.ContainerAdapter;
import io.dekorate.kubernetes.config.Container;

/**
 * A decorator that adds an init container to a pod template.
 */
@Description("Add an init container to a pod template.")
public class AddInitContainerDecorator extends NamedResourceDecorator<PodSpecBuilder> {

  private final Container container;

  public AddInitContainerDecorator(String deployment, Container container) {
    super(deployment);
    this.container = container;
  }

  @Override
  public void andThenVisit(PodSpecBuilder podSpec, ObjectMeta resourceMeta) {
    podSpec.addToInitContainers(ContainerAdapter.adapt(container));
  }
}
