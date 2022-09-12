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
package io.dekorate.openshift.annotation;

import io.sundr.builder.annotations.Pojo;

@Pojo(relativePath = "../config", autobox = true, mutable = true, withStaticBuilderMethod = true, withStaticAdapterMethod = false)
public @interface Route {

  /**
   * Controls whether the application should be exposed via Route
   */
  boolean expose() default false;

  /**
   * The host under which the application is going to be exposed.
   *
   * @return The hostname.
   */
  String host() default "";

  /**
   * @return The target named port. If not provided, it will be deducted from the Service resource ports.
   */
  String targetPort() default "";

}
