package io.dekorate.servicebinding.model;

import io.dekorate.deps.jackson.annotation.JsonInclude;
import io.dekorate.deps.jackson.annotation.JsonPropertyOrder;
import io.dekorate.deps.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "containerName", "divisor", "resource" })
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder")
public class ResourceFieldRef {

  private String containerName;
  private String divisor;
  private String resource;

  public ResourceFieldRef(String containerName, String divisor, String resource) {
    super();
    this.containerName = containerName;
    this.divisor = divisor;
    this.resource = resource;
  }

  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  public String getDivisor() {
    return divisor;
  }

  public void setDivisor(String divisor) {
    this.divisor = divisor;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

}
