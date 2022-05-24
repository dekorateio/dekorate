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
public @interface CertificatePrivateKey {
  /**
   * RotationPolicy controls how private keys should be regenerated when a re-issuance is being processed.
   * 
   * @return the rotation policy.
   */
  RotationPolicy rotationPolicy() default RotationPolicy.Unset;

  /**
   * @return the private key cryptography standards (PKCS) encoding for this certificate’s private key to be encoded in.
   */
  PrivateKeyEncoding encoding() default PrivateKeyEncoding.Unset;

  /**
   * @return the private key algorithm of the corresponding private key for this certificate.
   */
  PrivateKeyAlgorithm algorithm() default PrivateKeyAlgorithm.Unset;

  /**
   * @return the key bit size of the corresponding private key for this certificate.
   */
  int size() default -1;
}
