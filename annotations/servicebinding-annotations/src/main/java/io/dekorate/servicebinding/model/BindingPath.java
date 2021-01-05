package io.dekorate.servicebinding.model;

import io.dekorate.deps.jackson.annotation.JsonInclude;
import io.dekorate.deps.jackson.annotation.JsonPropertyOrder;
import io.dekorate.deps.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "containerPath", "secretPath" })
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder")
public class BindingPath {

  private String containerPath;
  private String secretPath;

  public BindingPath(String containerPath, String secretPath) {
    super();
    this.containerPath = containerPath;
    this.secretPath = secretPath;
  }

  public String getContainerPath() {
    return containerPath;
  }

  public void setContainerPath(String containerPath) {
    this.containerPath = containerPath;
  }

  public String getSecretPath() {
    return secretPath;
  }

  public void setSecretPath(String secretPath) {
    this.secretPath = secretPath;
  }
}
