package io.dekorate.servicebinding.model;

import io.dekorate.deps.jackson.annotation.JsonInclude;
import io.dekorate.deps.jackson.annotation.JsonPropertyOrder;
import io.dekorate.deps.jackson.databind.annotation.JsonDeserialize;

import io.sundr.builder.annotations.Buildable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "group", "kind", "name", "version", "id" })
@JsonDeserialize(using = io.dekorate.deps.jackson.databind.JsonDeserializer.None.class)
@Buildable(editableEnabled = false, builderPackage = "io.dekorate.deps.kubernetes.api.builder")
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
