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

import io.sundr.builder.annotations.Pojo;

@Pojo(relativePath = "../config", autobox = true, mutable = true, withStaticBuilderMethod = true, withStaticAdapterMethod = false)
public @interface IngressRule {

  /**
   * The host under which the rule is going to be used.
   */
  String host();

  /**
   * The path under which the rule is going to be used. Default is "/".
   */
  String path() default "/";

  /**
   * The path type strategy to use by the Ingress rule. Default is "Prefix".
   */
  String pathType() default "Prefix";

  /**
   * The service name to be used by this Ingress rule. Default is the generated service name of the application.
   */
  String serviceName() default "";

  /**
   * The service port name to be used by this Ingress rule. Default is the port name of the generated service
   * of the application.
   */
  String servicePortName() default "";

  /**
   * The service port number to be used by this Ingress rule. This is only used when the servicePortName is not set.
   */
  int servicePortNumber() default -1;

}
