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

/**
 * Interface to resolve properties that were handled by decorators from final manifests using JSON Path.
 */
public interface WithConfigReference {
  /**
   * @return key name of the config reference to resolve.
   */
  String getConfigReference();

  /**
   * @return json path to resolve the property in the generated JSON manifest.
   */
  String getJsonPathProperty();

  /**
   * If the value is null, then the framework will check the actual value of the generated JSON manifest.
   * 
   * @return value of the config reference.
   */
  default Object getConfigValue() {
    return null;
  }
}
