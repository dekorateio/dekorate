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
public @interface CertificateKeystores {
  /**
   * JKS configures options for storing a JKS keystore in the spec.secretName Secret resource.
   * If set, a file named keystore.jks will be created in the target Secret resource, encrypted using the password stored in
   * passwordSecretRef.
   * 
   * @return the JKS configuration.
   */
  CertificateKeystore jks() default @CertificateKeystore;

  /**
   * PKCS12 configures options for storing a PKCS12 keystore in the spec.secretName Secret resource.
   * If set, a file named keystore.p12 will be created in the target Secret resource, encrypted using the password stored in
   * passwordSecretRef.
   * 
   * @return the PKCS12 configuration.
   */
  CertificateKeystore pkcs12() default @CertificateKeystore;
}
