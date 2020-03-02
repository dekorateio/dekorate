package io.dekorate.halkyon.model;

import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;
import io.dekorate.deps.kubernetes.api.model.Doneable;
import io.dekorate.deps.jackson.annotation.JsonInclude;
import io.dekorate.deps.jackson.annotation.JsonPropertyOrder;
import io.dekorate.deps.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "requires",
  "provides"
})
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class Capabilities {
  private RequiredComponentCapability[] requires;
  private ComponentCapability[] provides;

  public Capabilities() {
  }

  public Capabilities(RequiredComponentCapability[] requires, ComponentCapability[] provides) {
    this.requires = requires;
    this.provides = provides;
  }

  public RequiredComponentCapability[] getRequires() {
    return requires;
  }

  public void setRequires(RequiredComponentCapability[] requires) {
    this.requires = requires;
  }

  public ComponentCapability[] getProvides() {
    return provides;
  }

  public void setProvides(ComponentCapability[] provides) {
    this.provides = provides;
  }
}
