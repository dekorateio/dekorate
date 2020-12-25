package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "apiVersion", "fieldPath" })
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class FieldRef {

  private String apiVersion;
  private String fieldPath;

  public FieldRef(String apiVersion, String fieldPath) {
    super();
    this.apiVersion = apiVersion;
    this.fieldPath = fieldPath;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public String getFieldPath() {
    return fieldPath;
  }

  public void setFieldPath(String fieldPath) {
    this.fieldPath = fieldPath;
  }

}
