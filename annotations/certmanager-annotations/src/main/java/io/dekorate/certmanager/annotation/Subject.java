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
public @interface Subject {

  /**
   * @return The organizations to be used on the Certificate.
   */
  String[] organizations() default {};

  /**
   * @return The countries to be used on the Certificate.
   */
  String[] countries() default {};

  /**
   * @return The organizational Units to be used on the Certificate.
   */
  String[] organizationalUnits() default {};

  /**
   * @return The cities to be used on the Certificate.
   */
  String[] localities() default {};

  /**
   * @return The State/Provinces to be used on the Certificate.
   */
  String[] provinces() default {};

  /**
   * @return The street addresses to be used on the Certificate.
   */
  String[] streetAddresses() default {};

  /**
   * @return The postal codes to be used on the Certificate.
   */
  String[] postalCodes() default {};

  /**
   * @return The serial number to be used on the Certificate.
   */
  String serialNumber() default "";
}
