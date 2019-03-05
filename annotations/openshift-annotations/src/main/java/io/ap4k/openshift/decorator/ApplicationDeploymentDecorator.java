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
package io.ap4k.openshift.decorator;

import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.deps.kubernetes.api.model.apps.DeploymentBuilder;
import io.ap4k.deps.openshift.api.model.DeploymentConfigBuilder;
import io.ap4k.kubernetes.decorator.Decorator;

public abstract class ApplicationDeploymentDecorator<T> extends Decorator<DeploymentConfigBuilder> {

  private final String name;

  public ApplicationDeploymentDecorator(String name) {
    this.name = name;
  }

  @Override
  public void visit(DeploymentConfigBuilder deployment) {
    if (!deployment.hasMetadata() || !deployment.buildMetadata().getName().equals(name)) {
      return;
    }
    deployment.accept(new TypedVisitor<T>() {
      @Override
      public void visit(T item) {
        andThenVisit(item);
      }
    });
  }

  public abstract void andThenVisit(T item);

}
