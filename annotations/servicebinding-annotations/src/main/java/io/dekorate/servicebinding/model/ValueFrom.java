package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "configMapKeyRef", "secretKeyRef", "fieldRef", "resourceFieldRef" })
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class ValueFrom {

  private ConfigMapKeyRef configMapKeyRef;
  private FieldRef fieldRef;
  private ResourceFieldRef resourceFieldRef;
  private SecretKeyRef secretKeyRef;

  public ValueFrom(ConfigMapKeyRef configMapKeyRef, SecretKeyRef secretKeyRef, FieldRef fieldRef,
      ResourceFieldRef resourceFieldRef) {
    super();
    this.configMapKeyRef = configMapKeyRef;
    this.fieldRef = fieldRef;
    this.resourceFieldRef = resourceFieldRef;
    this.secretKeyRef = secretKeyRef;
  }

  public ConfigMapKeyRef getConfigMapKeyRef() {
    return configMapKeyRef;
  }

  public void setConfigMapKeyRef(ConfigMapKeyRef configMapKeyRef) {
    this.configMapKeyRef = configMapKeyRef;
  }

  public FieldRef getFieldRef() {
    return fieldRef;
  }

  public void setFieldRef(FieldRef fieldRef) {
    this.fieldRef = fieldRef;
  }

  public ResourceFieldRef getResourceFieldRef() {
    return resourceFieldRef;
  }

  public void setResourceFieldRef(ResourceFieldRef resourceFieldRef) {
    this.resourceFieldRef = resourceFieldRef;
  }

  public SecretKeyRef getSecretKeyRef() {
    return secretKeyRef;
  }

  public void setSecretKeyRef(SecretKeyRef secretKeyRef) {
    this.secretKeyRef = secretKeyRef;
  }

}
