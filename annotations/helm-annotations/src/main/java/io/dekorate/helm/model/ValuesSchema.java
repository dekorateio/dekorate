package io.dekorate.helm.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the Values Schema json file.
 * More information in <a href="https://helm.sh/docs/topics/charts/#schema-files">here</a>
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValuesSchema {
  @JsonProperty(value = "$schema")
  private String schema = "https://json-schema.org/draft-07/schema#";
  @JsonProperty
  private String type = "object";
  @JsonProperty
  private String title;
  @JsonProperty
  private Map<String, ValuesSchemaProperty> properties = new HashMap<>();
  @JsonProperty
  private Set<String> required = new HashSet<>();

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Map<String, ValuesSchemaProperty> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, ValuesSchemaProperty> properties) {
    this.properties = properties;
  }

  public Set<String> getRequired() {
    return required;
  }

  public void setRequired(Set<String> required) {
    this.required = required;
  }
}
