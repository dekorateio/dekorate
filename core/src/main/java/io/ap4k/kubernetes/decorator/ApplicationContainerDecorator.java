package io.ap4k.kubernetes.decorator;

import io.ap4k.deps.kubernetes.api.model.ContainerFluent;
import io.ap4k.utils.Strings;

/**
 * An abstract class for decorating the application container.
 * This is meant to be used by decorators that are intended to be applied only to the application container (e.g. skip sidecars).
 */
public abstract class ApplicationContainerDecorator extends Decorator<ContainerFluent<?>>  {

  private final String containerName;

  public ApplicationContainerDecorator(String containerName) {
    this.containerName = containerName;
  }

  protected boolean isApplicable(ContainerFluent<?> container) {
    return Strings.isNotNullOrEmpty(containerName) && containerName.equals(container.getName());
  }
}
