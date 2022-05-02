/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.dekorate.certmanager.annotation;

import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.Pojo;

@Buildable(builderPackage = "io.fabric8.kubernetes.api.builder")
@Pojo(relativePath = "../config", autobox = true, mutable = true, withStaticMapAdapterMethod = true, withStaticAdapterMethod = false)
public @interface VaultAppRole {

  /**
   * @return the App Role authentication backend is mounted in Vault, e.g: “approle”
   */
  String path() default "";

  /**
   * @return the App Role authentication backend when setting up the authentication backend in Vault.
   */
  String roleId() default "";

  /**
   * @return the reference to a key in a Secret that contains the App Role secret used to authenticate with Vault.
   */
  LocalObjectReference secretRef() default @LocalObjectReference;
}
