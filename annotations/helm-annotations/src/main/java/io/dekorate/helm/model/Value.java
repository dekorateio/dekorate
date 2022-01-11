package io.dekorate.helm.model;

public class Value {

  private String property;
  private String jsonPath;
  private Object value;

  public Value(String property, String jsonPath, Object value) {
    this.property = property;
    this.jsonPath = jsonPath;
    this.value = value;
  }

  public String getProperty() {
    return property;
  }

  public String getJsonPath() {
    return jsonPath;
  }

  public Object getValue() {
    return value;
  }
}
