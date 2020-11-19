package io.dekorate.servicebinding.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.Doneable;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Inline;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "group", "kind", "name", "version", "id" })
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder", inline = @Inline(type = Doneable.class, prefix = "Doneable", value = "done"))
public class Service {

  private String group;
  private String kind;
  private String name;
  private String version;
  private String id;
  private String namespace;
  private String envVarPrefix;

  public Service(String group, String kind, String name, String version, String id, String namespace,
      String envVarPrefix) {
    super();
    this.group = group;
    this.kind = kind;
    this.name = name;
    this.version = version;
    this.id = id;
    this.namespace = namespace;
    this.envVarPrefix = envVarPrefix;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getEnvVarPrefix() {
    return envVarPrefix;
  }

  public void setEnvVarPrefix(String envVarPrefix) {
    this.envVarPrefix = envVarPrefix;
  }

}
