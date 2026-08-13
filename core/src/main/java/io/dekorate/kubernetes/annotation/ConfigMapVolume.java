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

public @interface ConfigMapVolume {

  /**
   * The volumeName name.
   * 
   * @return The volumeName name.
   */
  String volumeName();

  /**
   * The name of the config map to mount.
   * 
   * @return The name.
   */
  String configMapName();

  /**
   * Default mode.
   * 
   * @return The default mode.
   */
  int defaultMode() default 0600;

  /**
   * Optional
   * 
   * @return True if optional, False otherwise.
   */
  boolean optional() default false;

  /**
   * Optional
   *
   * @return list of files to be mounted.
   */
  Item[] items() default {};

}
