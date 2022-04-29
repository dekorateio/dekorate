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

package io.dekorate.knative.decorator;

import java.util.List;
import java.util.stream.Collectors;

import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddLivenessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddReadinessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddStartupProbeDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.fabric8.knative.serving.v1.RevisionSpecFluent;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class RemoveProbesFromRevisionSpecDecorator extends NamedResourceDecorator<RevisionSpecFluent<?>> {

  public RemoveProbesFromRevisionSpecDecorator() {
    this(ANY);
  }

  public RemoveProbesFromRevisionSpecDecorator(String resourceName) {
    super(resourceName);
  }

  @Override
  public void andThenVisit(RevisionSpecFluent<?> spec, ObjectMeta meta) {
    // At the momement Container.withXXXProbe(null) is broken for null values we need to workaround that and use the container object directly.;
    List<Container> containers = spec.buildContainers().stream().map(c -> {
      c.setReadinessProbe(null);
      c.setLivenessProbe(null);
      c.setStartupProbe(null);
      c.setLifecycle(null);
      return c;
    }).collect(Collectors.toList());
    spec.withContainers(containers);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddInitContainerDecorator.class, AddLivenessProbeDecorator.class,
        AddReadinessProbeDecorator.class, AddStartupProbeDecorator.class };
  }
}
