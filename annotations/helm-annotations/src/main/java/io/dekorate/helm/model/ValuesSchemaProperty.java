package io.dekorate.helm.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValuesSchemaProperty {
  @JsonProperty
  private String description;
  @JsonProperty
  private String type;
  @JsonProperty
  private Integer minimum;
  @JsonProperty
  private Integer maximum;
  @JsonProperty
  private String pattern;
  @JsonProperty(value = "enum")
  private Set<String> enumValues;
  @JsonProperty
  private Map<String, ValuesSchemaProperty> properties = new HashMap<>();
  @JsonProperty
  private Set<String> required = new HashSet<>();

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Set<String> getEnumValues() {
    return enumValues;
  }

  public void setEnumValues(Set<String> enumValues) {
    this.enumValues = enumValues;
  }

  public Integer getMaximum() {
    return maximum;
  }

  public void setMaximum(Integer maximum) {
    this.maximum = maximum;
  }

  public Integer getMinimum() {
    return minimum;
  }

  public void setMinimum(Integer minimum) {
    this.minimum = minimum;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
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
