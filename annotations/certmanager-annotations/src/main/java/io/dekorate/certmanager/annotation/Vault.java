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
public @interface Vault {

  /**
   * @return the connection address for the Vault server, e.g: “https://vault.example.com:8200”.
   */
  String server() default "";

  /**
   * @return the mount path of the Vault PKI backend’s sign endpoint, e.g: “my_pki_mount/sign/my-role-name”.
   */
  String path() default "";

  /**
   * Vault authentication using token.
   * 
   * @return the reference where to retrieve the Vault token.
   */
  LocalObjectReference authTokenSecretRef() default @LocalObjectReference;

  /**
   * Vault authentication using App Role auth mechanism.
   * 
   * @return the app role authentication configuration.
   */
  VaultAppRole authAppRole() default @VaultAppRole;

  /**
   * Vault authentication using Kubernetes service account.
   * 
   * @return the kubernetes authentication configuration.
   */
  VaultKubernetesAuth authKubernetes() default @VaultKubernetesAuth;

  /**
   * @return the vault namespace.
   */
  String namespace() default "";

  /**
   * @return the PEM-encoded CA bundle (base64-encoded) used to validate Vault server certificate.
   */
  String caBundle() default "";
}
