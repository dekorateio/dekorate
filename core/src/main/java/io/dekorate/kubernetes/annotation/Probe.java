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

public @interface Probe {

  /**
   * The http path to use for the probe
   * For this to work, the container port also needs to be set
   *
   * Assuming the container port has been set (as per above comment),
   * if execAction or tcpSocketAction are not set, an http probe
   * will be used automatically even if no path is set (which will
   * result in the root path being used)
   */
  String httpActionPath() default "";

  /**
   * The command to use for the probe.
   * @return  The command.
   */
  String execAction() default "";

  /**
   * The tcp socket to use for the probe (the format is host:port).
   * @return  The string representation of the socket.
   */
  String tcpSocketAction() default "";

  /**
   * The amount of time to wait in seconds before starting to probe.
   * @return  The initial delay.
   */
  int initialDelaySeconds() default 0;

  /**
   * The period in which the action should be called.
   * @return  The period.
   */
  int periodSeconds() default 30;

  /**
   * The amount of time to wait for each action.
   * @return  The timeout.
   */
  int timeoutSeconds() default 10;


  /**
   * The success threshold to use.
   * @return The threshold.
   */
  int successThreshold() default 1;


  /**
   * The failure threshold to use.
   * @return The threshold.
   */
  int failureThreshold() default 3;

}

