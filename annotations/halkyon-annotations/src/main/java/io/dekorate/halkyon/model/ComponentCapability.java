package io.dekorate.halkyon.model;

import io.dekorate.deps.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class ComponentCapability {

    private String name;
    private CapabilitySpec spec;


  public ComponentCapability() {
  }

  public ComponentCapability(String name, CapabilitySpec spec) {
    this.name = name;
    this.spec = spec;
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
}
