package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "value", "valueFrom" })
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class CustomEnvVar {

  private String name;
  private String value;
  private ValueFrom valueFrom;

  public CustomEnvVar() {
  }

  public CustomEnvVar(String name, String value, ValueFrom valueFrom) {
    super();
    this.name = name;
    this.value = value;
    this.valueFrom = valueFrom;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public ValueFrom getValueFrom() {
    return valueFrom;
  }

  public void setValueFrom(ValueFrom valueFrom) {
    this.valueFrom = valueFrom;
  }
}
