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
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ContainerFluent;

@Description("A decorator that applies the working directory to the application container.")
public class ApplyWorkingDirDecorator extends ApplicationContainerDecorator<ContainerFluent<?>> {

  private final String workingDir;

  public ApplyWorkingDirDecorator(String containerName, String workingDir) {
    super(null, containerName);
    this.workingDir = workingDir;
  }

  @Override
  public void andThenVisit(ContainerFluent<?> container) {
    if (isApplicable(container) && Strings.isNotNullOrEmpty(workingDir)) {
      container.withWorkingDir(workingDir);
    }
  }
}
