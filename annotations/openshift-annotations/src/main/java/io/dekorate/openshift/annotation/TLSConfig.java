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

public @interface TLSConfig {

  /**
   * @return the cert authority certificate contents.
   */
  String caCertificate() default "";

  /**
   * @return the certificate contents.
   */
  String certificate() default "";

  /**
   * @return the contents of the ca certificate of the final destination.
   */
  String destinationCACertificate() default "";

  /**
   * @return the desired behavior for insecure connections to a route. Options are: `allow`, `disable`, and `redirect`.
   */
  String insecureEdgeTerminationPolicy() default "";

  /**
   * @return the key file contents.
   */
  String key() default "";

  /**
   * @return the termination type.
   */
  String termination() default "";

}
