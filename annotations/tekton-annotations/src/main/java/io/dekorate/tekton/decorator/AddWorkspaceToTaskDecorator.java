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

import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;

public class AddWorkspaceToTaskDecorator extends NamedResourceDecorator<TaskSpecFluent<?>> {

  private final String id;
  private final String description;
  private final boolean readOnly;
  private final String path;

  public AddWorkspaceToTaskDecorator(String name, String id, String description, boolean readOnly, String path) {
    super(name);
    this.id = id;
    this.description = description;
    this.readOnly = readOnly;
    this.path = path;
  }

  @Override
  public void andThenVisit(TaskSpecFluent<?> task, ObjectMeta resourceMeta) {
    task.addNewWorkspace().withName(id).withDescription(description).withReadOnly(readOnly).withMountPath(path)
        .endWorkspace();
  }
}
