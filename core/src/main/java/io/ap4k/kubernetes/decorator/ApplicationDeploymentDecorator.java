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
package io.ap4k.kubernetes.decorator;

import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.deps.kubernetes.api.builder.VisitableBuilder;
import io.ap4k.deps.kubernetes.api.model.ObjectMeta;
import io.ap4k.utils.Generics;
import io.ap4k.utils.Strings;

import java.util.Optional;

import static io.ap4k.utils.Metadata.getMetadata;

public abstract class ApplicationDeploymentDecorator<T> extends Decorator<VisitableBuilder> {
  /**
   * For deployment name null acts as a wildcards.
   * Let's use a constant instead, for clarity's shake
   */
  public static final String ANY = null;

  protected final String deploymentName;

  private final DeploymentVisitor deploymentVisitor = new DeploymentVisitor();

  public ApplicationDeploymentDecorator(String deploymentName) {
    this.deploymentName = deploymentName;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (Strings.isNullOrEmpty(deploymentName) || objectMeta.map(m -> m.getName()).filter(s -> s.equals(deploymentName)).isPresent()) {
      builder.accept(deploymentVisitor);
    }
  }

  public abstract void andThenVisit(T item);

  private class DeploymentVisitor extends TypedVisitor<T> {

    @Override
    public void visit(T item) {
     andThenVisit(item);
    }

    public Class<T> getType() {
      return (Class)Generics.getTypeArguments(ApplicationDeploymentDecorator.class, ApplicationDeploymentDecorator.this.getClass()).get(0);
    }
  }
}
