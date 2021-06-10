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

package io.dekorate.kubernetes.decorator;

import io.dekorate.utils.Predicates;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpecFluent;

public class RemoveProbesFromInitContainerDecorator extends NamedResourceDecorator<PodSpecFluent<?>> {

  @Override
  public void andThenVisit(PodSpecFluent<?> podSpec, ObjectMeta resourceMeta) {
    podSpec.getContainers().forEach(container -> {
      podSpec.editMatchingContainer(Predicates.builderMatches(container))
          .withLivenessProbe(null)
          .withReadinessProbe(null)
          .withLifecycle(null)
          .withStartupProbe(null);
    });
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddInitContainerDecorator.class, AddLivenessProbeDecorator.class,
        AddReadinessProbeDecorator.class };
  }
}
