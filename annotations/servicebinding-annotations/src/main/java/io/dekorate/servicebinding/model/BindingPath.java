package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "containerPath", "secretPath" })
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
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
