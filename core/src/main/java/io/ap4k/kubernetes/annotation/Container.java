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
 *
 **/

package io.ap4k.kubernetes.annotation;

public @interface Container {

  /**
   * The container image.
   * @return  The container image.
   */
  String image();

  /**
   * The container name.
   * @return The container name, or the name part of the image, if no name has been specified.
   */
  String name() default "";

  /**
   * Environment variables to add to all containers.
   * @return The environment variables.
   */
  Env[] envVars() default {};

  /**
   * Working directory.
   * @return The working directory if specified, else empty string.
   */
  String workingDir() default "";

  /**
   * The commands
   * @return The commands.
   */
  String[] command() default {};

  /**
   * The arguments
   * @return The arguments.
   */
  String[] arguments() default {};


  /**
   * The application ports.
   */
  Port[] ports() default {};

  /**
   * Mounts to add to all containers.
   * @return  The mounts.
   */
  Mount[] mounts() default {};

  /**
   * Image pull policy.
   * @return The image pull policy.
   */
  ImagePullPolicy imagePullPolicy() default ImagePullPolicy.IfNotPresent;


  /**
   * The liveness probe.
   * @return  The probe.
   */
  Probe livenessProbe() default @Probe();

  /**
   * The readiness probe.
   * @return  The probe.
   */
  Probe readinessProbe() default @Probe();

}
