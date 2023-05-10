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
import io.fabric8.kubernetes.api.model.ContainerFluent;

@Description("A decorator that applies the command to the application container.")
public class ApplyCommandDecorator extends ApplicationContainerDecorator<ContainerFluent> {

  private final String[] command;

  public ApplyCommandDecorator(String containerName, String... command) {
    super(null, containerName);
    this.command = command;
  }

  public ApplyCommandDecorator(String deployment, String container, String... command) {
    super(deployment, container);
    this.command = command;
  }

  @Override
  public void andThenVisit(ContainerFluent container) {
    if (isApplicable(container) && command != null && command.length > 0) {
      container.withCommand(command);
    }
  }
}
