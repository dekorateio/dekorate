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

import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpecFluent;

public class RemoveProbesDecorator extends ApplicationContainerDecorator<PodSpecFluent<?>> {

  public RemoveProbesDecorator() {
    this(ANY, ANY);
  }

  public RemoveProbesDecorator(String resourceName) {
    this(resourceName, ANY);
  }

  public RemoveProbesDecorator(String resourceName, String containerName) {
    super(resourceName, containerName);
  }

  @Override
  public void andThenVisit(PodSpecFluent<?> podSpec) {
    // At the momement Container.withXXXProbe(null) is broken for null values we need to workaround that and use the container object directly.;
    List<Container> containers = podSpec.buildContainers().stream().map(c -> {
      c.setReadinessProbe(null);
      c.setLivenessProbe(null);
      c.setStartupProbe(null);
      c.setLifecycle(null);
      return c;
    }).collect(Collectors.toList());
    podSpec.withContainers(containers);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddInitContainerDecorator.class, AddLivenessProbeDecorator.class,
        AddReadinessProbeDecorator.class, AddStartupProbeDecorator.class };
  }
}
