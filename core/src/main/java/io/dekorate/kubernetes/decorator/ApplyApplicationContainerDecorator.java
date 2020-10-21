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

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.adapter.ContainerAdapter;
import io.dekorate.kubernetes.config.Container;
import io.dekorate.utils.Predicates;
import io.dekorate.deps.kubernetes.api.builder.Predicate;
import io.dekorate.deps.kubernetes.api.model.ContainerBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.PodSpecBuilder;

/**
 * A decorator that adds a the container if no matching container found.
 */
@Description("A decorator that adds a the container if no matching container found")
public class ApplyApplicationContainerDecorator extends NamedResourceDecorator<PodSpecBuilder> {

  private final Container container;

  public ApplyApplicationContainerDecorator(Container container) {
    this(ANY, container);
  }

  public ApplyApplicationContainerDecorator(String deployment, Container container) {
    super(deployment);
    this.container = container;
  }

  @Override
  public void andThenVisit(PodSpecBuilder podSpec, ObjectMeta resourceMeta) {
    Predicate<ContainerBuilder> p = Predicates.builderMatches(container);
    if (!podSpec.hasMatchingContainer(p)) {
      podSpec.addToContainers(ContainerAdapter.adapt(container));
    } else {
      ContainerBuilder builder = new ContainerBuilder(podSpec.buildMatchingContainer(p));
      podSpec.removeMatchingFromContainers(p);
      ContainerAdapter.applyContainerToBuilder(builder, container);
      podSpec.addToContainers(builder.build());
    }
  }

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

}
