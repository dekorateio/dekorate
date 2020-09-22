package io.dekorate.halkyon.decorator;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.dekorate.halkyon.config.CapabilityConfig;
import io.dekorate.halkyon.config.RequiredCapabilityConfig;
import io.dekorate.halkyon.model.ComponentCapability;
import io.dekorate.halkyon.model.ComponentCapabilityBuilder;
import io.dekorate.halkyon.model.ComponentSpecBuilder;
import io.dekorate.halkyon.model.Parameter;
import io.dekorate.halkyon.model.RequiredComponentCapability;
import io.dekorate.halkyon.model.RequiredComponentCapabilityBuilder;
import io.dekorate.kubernetes.decorator.Decorator;

public class AddCapabilitiesToComponentDecorator extends Decorator<ComponentSpecBuilder> {

  private final RequiredCapabilityConfig[] requires;
  private final CapabilityConfig[] provides;

  public AddCapabilitiesToComponentDecorator(RequiredCapabilityConfig[] requires, CapabilityConfig[] provides) {
    this.requires = requires;
    this.provides = provides;
  }

  @Override
  public void visit(ComponentSpecBuilder component) {

    component.withNewCapabilities()
        .addAllToProvides(Arrays.stream(provides)
            .map(this::createComponentCapability)
            .collect(Collectors.toList()))
        .addAllToRequires(Arrays.stream(requires)
            .map(this::createRequiredComponentCapability)
            .collect(Collectors.toList()))
        .endCapabilities();

  }

  /**
   * Create a {@link ComponentCapability} from a {@link CapabilityConfig}.
   *
   * @param config The config.
   * @return The ComponentCapability.
   */
  private ComponentCapability createComponentCapability(CapabilityConfig config) {
    return new ComponentCapabilityBuilder()
        .withName(config.getName())
        .withNewSpec()
        .withCategory(config.getCategory())
        .withType(config.getType())
        .withVersion(config.getVersion())
        .addAllToParameters(Arrays.stream(config.getParameters())
            .map(p -> new Parameter(p.getName(), p.getValue()))
            .collect(Collectors.toList()))
        .endSpec()
        .build();
  }

  /**
   * Create a {@link RequiredComponentCapability} from a {@link RequiredCapabilityConfig}.
   *
   * @param config The config.
   * @return The RequiredCapability.
   */
  private RequiredComponentCapability createRequiredComponentCapability(RequiredCapabilityConfig config) {

    return new RequiredComponentCapabilityBuilder()
        .withBoundTo(config.getBoundTo())
        .withName(config.getName())
        .withAutoBindable(config.isAutoBindable())
        .withNewSpec()
        .withCategory(config.getCategory())
        .withType(config.getType())
        .addAllToParameters(Arrays.stream(config.getParameters())
            .map(p -> new Parameter(p.getName(), p.getValue()))
            .collect(Collectors.toList()))
        .endSpec()
        .build();
  }

}
