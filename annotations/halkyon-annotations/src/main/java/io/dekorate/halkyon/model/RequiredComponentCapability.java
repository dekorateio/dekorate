package io.dekorate.halkyon.model;

import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import io.dekorate.deps.kubernetes.api.model.Doneable;

@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class RequiredComponentCapability {

  private String name;
  private String boundTo;
  private boolean autoBindable;
  private CapabilitySpec spec;

  public RequiredComponentCapability() {
  }

  public RequiredComponentCapability(String boundTo, boolean autoBindable, String name) {
    this.boundTo = boundTo;
    this.autoBindable = autoBindable;
    this.name = name;
  }

  public String getBoundTo() {
    return boundTo;
  }

  public void setBoundTo(String boundTo) {
    this.boundTo = boundTo;
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

  public boolean isAutoBindable() {
    return autoBindable;
  }

  public void setAutoBindable(boolean autoBindable) {
    this.autoBindable = autoBindable;
  }
}
