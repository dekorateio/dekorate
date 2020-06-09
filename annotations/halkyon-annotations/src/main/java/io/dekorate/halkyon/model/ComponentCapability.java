package io.dekorate.halkyon.model;

import io.fabric8.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class ComponentCapability {

  private String name;
  private CapabilitySpec spec;
  private Parameter[] parameters;

  public ComponentCapability() {
  }

  public ComponentCapability(String name, CapabilitySpec spec, Parameter[] parameters) {
    this.name = name;
    this.spec = spec;
    this.parameters = parameters;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CapabilitySpec getSpec() {
    return spec;
  }

  public void setSpec(CapabilitySpec spec) {
    this.spec = spec;
  }

  public Parameter[] getParameters() {
    return parameters;
  }

  public void setParameters(Parameter[] parameters) {
    this.parameters = parameters;
  }
}
