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

import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(relativePath = "../config", autobox = true, mutable = true, withStaticBuilderMethod = true, withStaticAdapterMethod = false)
public @interface Item {

  /**
   * The key name within the data section (in Secrets and ConfigMaps).
   * 
   * @return The key.
   */
  String key();

  /**
   * The path where the file will be mounted.
   * 
   * @return The path.
   */
  String path();

  /**
   * It must be a value between 0000 and 0777. If not specified, the volume defaultMode will be used.
   *
   * @return The file mode.
   */
  int mode() default -1;

}
