package io.dekorate.halkyon.model;

import io.fabric8.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class RequiredComponentCapability extends ComponentCapability {
  private String boundTo;
  private boolean autoBindable;

  public RequiredComponentCapability() {
  }

  public RequiredComponentCapability(String name, CapabilitySpec spec, Parameter[] parameters, String boundTo,
      boolean autoBindable) {
    super(name, spec, parameters);
    this.boundTo = boundTo;
    this.autoBindable = autoBindable;
  }

  public String getBoundTo() {
    return boundTo;
  }

  public void setBoundTo(String boundTo) {
    this.boundTo = boundTo;
  }

  public boolean isAutoBindable() {
    return autoBindable;
  }

  public void setAutoBindable(boolean autoBindable) {
    this.autoBindable = autoBindable;
  }
}
