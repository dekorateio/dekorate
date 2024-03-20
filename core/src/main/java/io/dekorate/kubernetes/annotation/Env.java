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
package io.dekorate.kubernetes.annotation;

public @interface Env {

  /**
   * The name of the environment variable.
   * 
   * @return the name.
   */
  String name();

  /**
   * The value of the environment variable. When no other fields are used (just
   * name/value), this value will be assigned to the environment variable. If used
   * with other fields, like secret, configmap, or field, it will indicate the key
   * from with the value should be drawn.
   * 
   * @return The value of the variable, or the property/key from which the value
   *         will be pulled (in case of secret, configmap or field).
   */
  String value() default "";

  String secret() default "";

  String configmap() default "";

  String field() default "";

  String resourceField() default "";

  String prefix() default "";
}
