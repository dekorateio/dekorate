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

import static io.dekorate.utils.Metadata.getMetadata;

import java.util.Optional;

import io.dekorate.utils.Generics;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * An abstract class for decorating the application container.
 * This is meant to be used by decorators that are intended to be applied only to the application container (e.g. skip
 * sidecars).
 */
public abstract class ApplicationContainerDecorator<T> extends Decorator<VisitableBuilder> {

  private final String deploymentName;
  private final String containerName;

  private final DeploymentVisitor deploymentVisitor = new DeploymentVisitor();
  private final ContainerVisitor containerVisitor = new ContainerVisitor();

  public ApplicationContainerDecorator() {
    this(ANY, ANY);
  }

  public ApplicationContainerDecorator(String containerName) {
    this(ANY, containerName);
  }

  public ApplicationContainerDecorator(String deploymentName, String containerName) {
    this.deploymentName = deploymentName;
    this.containerName = containerName;
  }

  protected String getDeploymentName() {
    return deploymentName;
  }

  protected String getContainerName() {
    return containerName;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (Strings.isNotNullOrEmpty(deploymentName) && !objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(deploymentName)
        || objectMeta.map(m -> m.getName()).filter(s -> s.equals(deploymentName)).isPresent()) {
      builder.accept(deploymentVisitor);
    }
  }

  protected boolean isApplicable(ContainerFluent<?> container) {
    return Strings.isNullOrEmpty(containerName) || containerName.equals(container.getName());
  }

  public abstract void andThenVisit(T item);

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class };
  }

  private class DeploymentVisitor extends TypedVisitor<ContainerBuilder> {

    @Override
    public void visit(ContainerBuilder container) {
      if (!isApplicable(container)) {
        return;
      }

      container.accept(containerVisitor);
    }
  }

  private class ContainerVisitor extends TypedVisitor<T> {
    @Override
    public void visit(T item) {
      andThenVisit(item);
    }

    public Class<T> getType() {
      return (Class) Generics
          .getTypeArguments(ApplicationContainerDecorator.class, ApplicationContainerDecorator.this.getClass())
          .get(0);
    }
  }
}
