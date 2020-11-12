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
import io.dekorate.utils.Generics;
import io.dekorate.utils.Strings;
import io.dekorate.deps.kubernetes.api.builder.BaseFluent;
import io.dekorate.deps.kubernetes.api.builder.TypedVisitor;
import io.dekorate.deps.kubernetes.api.builder.VisitableBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.tekton.pipeline.v1beta1.StepBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.StepFluent;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;

public abstract class NamedStepDecorator<T> extends Decorator<VisitableBuilder> {

  protected static final String ANY = null;

  private final String taskName;
  private final String stepName;

  private final TaskVisitor taskVisitor = new TaskVisitor();
  private final StepVisitor stepVisitor = new StepVisitor();

  public NamedStepDecorator(String taskName, String stepName) {
    this.taskName = taskName;
    this.stepName = stepName;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    BaseFluent f;
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (!objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(taskName)
        || objectMeta.map(m -> m.getName()).filter(s -> s.equals(taskName)).isPresent()) {
      builder.accept(taskVisitor);
    }
  }

  public abstract void andThenVisit(T item);

  private class TaskVisitor extends TypedVisitor<StepBuilder> {

    @Override
    public void visit(StepBuilder step) {
      if (Strings.isNullOrEmpty(stepName) || stepName.equals(step.getName())) {
        step.accept(stepVisitor);
      }
    }
  }

  private class StepVisitor extends TypedVisitor<T> {

    @Override
    public void visit(T item) {
      andThenVisit(item);
    }

    public Class<T> getType() {
      return (Class) Generics.getTypeArguments(NamedStepDecorator.class, NamedStepDecorator.this.getClass()).get(0);
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
