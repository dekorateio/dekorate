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
package io.ap4k.jaeger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.CONSTRUCTOR, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Collector {

  /**
   * The collector host
   * @return The host.
   */
  String host() default "";

  /**
   * The collector name.
   * This is used if no host has been specified.
   * @return
   */
  String name() default "jaeger-collector";

  /**
   * The collector namespace.
   * This is used if no host has been specified.
   * @return
   */
  String namespace() default "";

  /**
   * The collector port.
   * @return  The collector port.
   */
  int port() default 14267;
}
