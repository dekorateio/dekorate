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
package io.dekorate.testing.annotation;

import io.dekorate.testing.CustomResourceCondition;
import io.dekorate.testing.ServicePresentCondition;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.dekorate.testing.Constants.ANY;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ TYPE })
@Retention(RUNTIME)
@ExtendWith(CustomResourceCondition.class)
public @interface OnCustomResourcePresentCondition {
  /**
   * The custom resource apiVersion.
   * @return  The apiVersion.
   */
  String apiVersion() default "v1";

  /**
   * The custom resource kind.
   * @return  The kind.
   */
  String kind();

  /**
   * The custom resource plural.
   * @return The plural. If not specified the kind will be pluralized and converted to lowercase.
   */
  String plural() default "";

  /**
   * The name of the service.
   * @return  The name. Defaults to ANY.
   */
  String name() default ANY;

  /**
   * The service namespace.
   * @return  The namespace. Defaults to ANY.
   */
  String namespace() default ANY;
}
