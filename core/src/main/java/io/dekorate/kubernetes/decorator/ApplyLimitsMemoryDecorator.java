/*
 * Copyright 2020 The original authors.
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

import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.Quantity;

public class ApplyLimitsMemoryDecorator extends ApplicationContainerDecorator<ContainerFluent<?>> {

  private static final String MEM = "memory";

  private final String amount;

  public ApplyLimitsMemoryDecorator(String containerName, String amount) {
    super(containerName);
    this.amount = amount;
  }

  public ApplyLimitsMemoryDecorator(String deploymentName, String containerName, String amount) {
    super(deploymentName, containerName);
    this.amount = amount;
  }

  @Override
  public void andThenVisit(ContainerFluent<?> container) {
    container.editOrNewResources().addToLimits(MEM, new Quantity(amount)).endResources();
  }
}
