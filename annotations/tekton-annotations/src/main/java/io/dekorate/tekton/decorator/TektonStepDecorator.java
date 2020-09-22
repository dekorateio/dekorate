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
package io.dekorate.tekton.decorator;

import static io.dekorate.utils.Metadata.getMetadata;

import java.util.Optional;

import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.utils.Generics;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.tekton.pipeline.v1beta1.StepBuilder;
import io.fabric8.tekton.pipeline.v1beta1.StepFluent;

public abstract class TektonStepDecorator<T> extends Decorator<VisitableBuilder> {

  /**
   * For container and deployment name null acts as a wildcards.
   * Let's use a constant instead, for clarity's shake
   */
  public static final String ANY = null;

  private final String taskName;
  private final String stepName;

  private final TaskVisitor deploymentVisitor = new TaskVisitor();
  private final StepVisitor stepVisitor = new StepVisitor();

  public TektonStepDecorator() {
    this(null, null);
  }

  public TektonStepDecorator(String stepName) {
    this(null, stepName);
  }

  public TektonStepDecorator(String taskName, String stepName) {
    this.taskName = taskName;
    this.stepName = stepName;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (!objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(taskName) || objectMeta.map(m -> m.getName()).filter(s -> s.equals(taskName)).isPresent()) {
      builder.accept(deploymentVisitor);
    }
  }

  protected boolean isApplicable(StepFluent<?> step) {
    return Strings.isNullOrEmpty(stepName) || stepName.equals(step.getName());
  }

  public abstract void andThenVisit(T item);

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

  private class TaskVisitor extends TypedVisitor<StepBuilder> {

    @Override
    public void visit(StepBuilder step) {
      if (!isApplicable(step)) {
        return;
      }

      step.accept(stepVisitor);
    }
  }

  private class StepVisitor extends TypedVisitor<T> {
    @Override
    public void visit(T item) {
      andThenVisit(item);
    }

    public Class<T> getType() {
      return (Class) Generics.getTypeArguments(TektonStepDecorator.class, TektonStepDecorator.this.getClass()).get(0);
    }
  }
}
