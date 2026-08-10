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
import io.dekorate.kubernetes.adapter.ContainerAdapter;
import io.dekorate.kubernetes.config.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodSpecFluent;

/**
 * A decorator that adds an init container to a pod template.
 */
@Description("Add an sidecar container to a pod template.")
public class AddSidecarDecorator extends NamedResourceDecorator<PodSpecBuilder> {

  private final Container container;

  public AddSidecarDecorator(Container container) {
    this(ANY, container);
  }

  public AddSidecarDecorator(String deployment, Container container) {
    super(deployment);
    this.container = container;
  }

  @Override
  public void andThenVisit(PodSpecBuilder podSpec, ObjectMeta resourceMeta) {
    io.fabric8.kubernetes.api.model.Container resource = ContainerAdapter.adapt(container);
    if (podSpec.hasMatchingContainer(this::existsContainerByName)) {
      update(podSpec, resource);
    } else {
      add(podSpec, resource);
    }
  }

  private void add(PodSpecBuilder podSpec, io.fabric8.kubernetes.api.model.Container resource) {
    podSpec.addToContainers(resource);
  }

  private void update(PodSpecBuilder podSpec, io.fabric8.kubernetes.api.model.Container resource) {
    PodSpecFluent<PodSpecBuilder>.ContainersNested<PodSpecBuilder> matching = podSpec
        .editMatchingContainer(this::existsContainerByName);

    if (resource.getImage() != null) {
      matching.withImage(resource.getImage());
    }

    if (resource.getImagePullPolicy() != null) {
      matching.withImagePullPolicy(resource.getImagePullPolicy());
    }

    if (resource.getWorkingDir() != null) {
      matching.withWorkingDir(resource.getWorkingDir());
    }

    if (resource.getCommand() != null && !resource.getCommand().isEmpty()) {
      matching.withCommand(resource.getCommand());
    }

    if (resource.getArgs() != null && !resource.getArgs().isEmpty()) {
      matching.withArgs(resource.getArgs());
    }

    if (resource.getReadinessProbe() != null) {
      matching.withReadinessProbe(resource.getReadinessProbe());
    }

    if (resource.getLivenessProbe() != null) {
      matching.withLivenessProbe(resource.getLivenessProbe());
    }

    matching.addAllToEnv(resource.getEnv());
    if (resource.getPorts() != null && !resource.getPorts().isEmpty()) {
      matching.withPorts(resource.getPorts());
    }

    matching.endContainer();
  }

  private boolean existsContainerByName(ContainerBuilder containerBuilder) {
    return containerBuilder.getName().equals(container.getName());
  }

  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class };
  }

}
