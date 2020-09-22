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

import static io.dekorate.utils.Metadata.getMetadata;

import java.util.Optional;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;

public abstract class NamedTaskDecorator extends Decorator<VisitableBuilder> {

  protected static final String ANY = null;

  private final String taskName;

  private final TaskVisitor taskVisitor = new TaskVisitor();

  public NamedTaskDecorator(String taskName) {
    this.taskName = taskName;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (!objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(taskName)
        || objectMeta.map(m -> m.getName()).filter(s -> s.equals(taskName)).isPresent()) {
      builder.accept(taskVisitor);
    }
  }

  public abstract void andThenVisit(TaskSpecFluent<?> taskSpec);

  private class TaskVisitor extends TypedVisitor<TaskSpecFluent<?>> {

    @Override
    public void visit(TaskSpecFluent<?> taskSpec) {
      andThenVisit(taskSpec);
    }
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, TaskProvidingDecorator.class };
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { TektonStepDecorator.class };
  }

}
