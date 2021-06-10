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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.fabric8.kubernetes.api.model.ContainerPortFluent;

public class ApplyPortNameDecorator extends ApplicationContainerDecorator<ContainerPortFluent<?>> {

  private final String name;
  private final Set<String> toReplace;

  public ApplyPortNameDecorator(String deploymentName, String containerName, String name, String... toReplace) {
    super(deploymentName, containerName);
    this.name = name;
    this.toReplace = new HashSet<String>(Arrays.asList(toReplace));
  }

  @Override
  public void andThenVisit(ContainerPortFluent<?> port) {
    if (toReplace.contains(port.getName())) {
      port.withName(name);
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class, AddSidecarDecorator.class,
        AddPortDecorator.class };
  }
}
