/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate;

import static io.dekorate.kubernetes.decorator.Decorator.ANY;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.dekorate.utils.Strings;

/**
 * A config reference is a way to find configuration values using json path from the generated manifests by Dekorate.
 * This can be handy for some extensions like Helm.
 */
public class ConfigReference {
  private final String property;
  private final String[] paths;
  private final Object value;
  private final String expression;
  private final String profile;
  private final String description;
  private final Integer minimum;
  private final Integer maximum;
  private final String pattern;
  private final Set<String> enumValues;
  private final boolean required;

  protected ConfigReference(String property, String[] paths, String description, Object value, String expression,
      String profile, Integer minimum, Integer maximum, String pattern, Set<String> enumValues, boolean required) {
    this.property = property;
    this.paths = paths;
    this.description = description;
    this.value = value;
    this.expression = expression;
    this.profile = profile;
    this.minimum = minimum;
    this.maximum = maximum;
    this.pattern = pattern;
    this.enumValues = enumValues;
    this.required = required;
  }

  /**
   * @return key name of the config reference to resolve.
   */
  public String getProperty() {
    return property;
  }

  /**
   * @return the expression paths to resolve the property in the generated YAML manifest.
   */
  public String[] getPaths() {
    return paths;
  }

  /**
   * If the value is null, then the framework will check the actual value of the generated JSON manifest.
   *
   * @return value of the config reference.
   */
  public Object getValue() {
    return value;
  }

  /**
   * If not provided, it will use `{{ .Values.<root alias>.<property> }}`.
   *
   * @return The complete Helm expression to be replaced with.
   */
  public String getExpression() {
    return expression;
  }

  /**
   * @return get the profile where the config reference belongs.
   */
  public String getProfile() {
    return profile;
  }

  /**
   * @return the description of the config reference.
   */
  public String getDescription() {
    return description;
  }

  public Integer getMinimum() {
    return minimum;
  }

  public Integer getMaximum() {
    return maximum;
  }

  public String getPattern() {
    return pattern;
  }

  public Set<String> getEnumValues() {
    return enumValues;
  }

  public boolean isRequired() {
    return required;
  }

  /**
   * Will generate a config reference name by appending the properties if they are not null or any.
   *
   * For example, if `properties` are [`first`, null, `image`], it will generate: `first.image`.
   */
  public static String joinProperties(String... properties) {
    if (properties == null) {
      return null;
    }

    return Strings.kebabToCamelCase(Stream.of(properties)
        .filter(p -> !Strings.equals(ANY, p))
        .collect(Collectors.joining(".")));
  }

  public static class Builder {
    private final String property;
    private final String[] paths;
    private String description;
    private Object value;
    private String expression;
    private String profile;
    private Integer minimum;
    private Integer maximum;
    private String pattern;
    private Set<String> enumValues;
    private boolean required = false;

    public Builder(String property) {
      this(property, new String[0]);
    }

    public Builder(String property, String path) {
      this(property, new String[] { path });
    }

    public Builder(String property, String[] paths) {
      this.property = property;
      this.paths = paths;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withValue(Object value) {
      this.value = value;
      return this;
    }

    public Builder withExpression(String expression) {
      this.expression = expression;
      return this;
    }

    public Builder withProfile(String profile) {
      this.profile = profile;
      return this;
    }

    public Builder withMinimum(int minimum) {
      if (minimum != Integer.MIN_VALUE) {
        this.minimum = minimum;
      }

      return this;
    }

    public Builder withMaximum(int maximum) {
      if (maximum != Integer.MAX_VALUE) {
        this.maximum = maximum;
      }

      return this;
    }

    public Builder withPattern(String pattern) {
      if (Strings.isNotNullOrEmpty(pattern)) {
        this.pattern = pattern;
      }

      return this;
    }

    public Builder withRequired(boolean required) {
      this.required = required;

      return this;
    }

    public <E extends Enum<E>> Builder withEnum(Class<E> enumType) {
      this.enumValues = new HashSet<>();
      for (E value : EnumSet.allOf(enumType)) {
        this.enumValues.add(value.name());
      }

      return this;
    }

    public ConfigReference build() {
      return new ConfigReference(property, paths, description, value, expression, profile, minimum, maximum, pattern,
          enumValues, required);
    }
  }
}
