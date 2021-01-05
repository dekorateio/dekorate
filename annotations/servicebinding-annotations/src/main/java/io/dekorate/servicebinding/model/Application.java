package io.dekorate.servicebinding.model;

import io.dekorate.deps.jackson.annotation.JsonInclude;
import io.dekorate.deps.jackson.annotation.JsonPropertyOrder;
import io.dekorate.deps.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "group", "resource", "name", "version", "bindingPath" })
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder")
public class Application {

  private String group;
  private String resource;
  private String name;
  private String version;
  private BindingPath bindingPath;

  public Application(String group, String resource, String name, String version, BindingPath bindingPath) {
    this.group = group;
    this.resource = resource;
    this.name = name;
    this.version = version;
    this.bindingPath = bindingPath;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public BindingPath getBindingPath() {
    return bindingPath;
  }

  public void setBindingPath(BindingPath bindingPath) {
    this.bindingPath = bindingPath;
  }

}
