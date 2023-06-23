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

public @interface RollingUpdate {

  /**
   * Specifies the maximum number of Pods that can be unavailable during the update process.
   */
  String maxUnavailable() default "25%";

  /**
   * Specifies the maximum number of Pods that can be created over the desired number of Pods.
   */
  String maxSurge() default "25%";

}
