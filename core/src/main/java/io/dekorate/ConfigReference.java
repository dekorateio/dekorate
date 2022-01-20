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

import io.dekorate.utils.Strings;

/**
 * A config reference is a way to find configuration values using json path from the generated manifests by Dekorate.
 * This can be handy for some extensions like Helm.
 */
public class ConfigReference {
  private String property;
  private String[] jsonPaths;
  private Object value;
  private String profile;

  public ConfigReference(String property, String jsonPath) {
    this(property, new String[] { jsonPath });
  }

  public ConfigReference(String property, String[] jsonPaths) {
    this(property, jsonPaths, null, null);
  }

  public ConfigReference(String property, String jsonPath, Object value) {
    this(property, new String[] { jsonPath }, value, null);
  }

  public ConfigReference(String property, String[] jsonPaths, Object value, String profile) {
    this.property = property;
    this.jsonPaths = jsonPaths;
    this.value = value;
    this.profile = profile;
  }

  /**
   * @return key name of the config reference to resolve.
   */
  public String getProperty() {
    return property;
  }

  /**
   * @return json path to resolve the property in the generated JSON manifest.
   */
  public String[] getJsonPaths() {
    return jsonPaths;
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
   * @return get the profile where the config reference belongs.
   */
  public String getProfile() {
    return profile;
  }

  /**
   * Will generate a config reference name based on the value in `base` and appending also the properties
   * if they are not null or any.
   *
   * For example, if `base` is `image` and `properties` are [`first`, null], it will generate: `first.image`.
   */
  public static String generateConfigReferenceName(String suffix, String... properties) {
    StringBuilder sb = new StringBuilder();
    for (String property : properties) {
      if (!Strings.equals(ANY, property)) {
        sb.append(property + ".");
      }
    }

    sb.append(suffix);

    return sb.toString();
  }
}
