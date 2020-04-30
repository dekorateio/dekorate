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

import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.Task;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskBuilder;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;

public class TaskProvidingDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final Task task;

  public TaskProvidingDecorator(String name) {
    this(name, null);
  }

  public TaskProvidingDecorator(Task task) {
    this(null, task);
  }

  public TaskProvidingDecorator(String name, Task task) {
    this.task = task != null ? task : new TaskBuilder()
      .withNewMetadata().withName(name).endMetadata()
      .withNewSpec().endSpec()
      .build();
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    list.addToItems(task);
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { NamedTaskDecorator.class };
  }

}
