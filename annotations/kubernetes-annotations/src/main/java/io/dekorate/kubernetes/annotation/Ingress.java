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
public @interface Ingress {

  /**
   * The host under which the application is going to be exposed.
   *
   * @return The hostname.
   */
  String host() default "";

  /**
   * The class of the Ingress. If the ingressClassName is omitted, a default Ingress class is used.
   *
   * @return The class of the Ingress.
   */
  String ingressClassName() default "";

  /**
   * @return The target port name to use. If not provided, it will be deducted from the Service resource ports.
   */
  String targetPort() default "http";

  /**
   * Controls whether the application should be exposed via Ingress
   */
  boolean expose() default false;

  /**
   * @return The name of the secret used to configure TLS.
   */
  String tlsSecretName() default "";

  /**
   * @return The list of hosts to be included in the TLS certificate. By default, it will use the application host.
   */
  String[] tlsHosts() default {};

  /**
   * Controls the generated ingress rules to be exposed as part of the Ingress resource.
   */
  IngressRule[] rules() default {};

}
