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

package io.dekorate.tekton.decorator;

import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;

public class AddResourceInputToTaskDecorator extends NamedTaskDecorator {

  private final String resourceType;
  private final String resourceName;

  public AddResourceInputToTaskDecorator(String taskName, String resourceType, String resourceName) {
    super(taskName);
    this.resourceType = resourceType;
    this.resourceName = resourceName;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> taskSpec) {
    if (!taskSpec.hasResources()) {
      taskSpec.withNewResources()
        .addNewInput()
        .withName(resourceName)
        .withType(resourceType)
        .endInput()
        .endResources();
    } else {
      taskSpec.editResources().removeMatchingFromInputs(r -> r.getName().equals(resourceName)).endResources();
      taskSpec.editResources()
        .addNewInput()
        .withName(resourceName)
        .withType(resourceType)
        .endInput()
        .endResources();
    }
  }
}
