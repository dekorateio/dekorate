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

package io.ap4k.annotation;

public @interface Probe {

  /**
   * A string representation of the URL on which to perform an http get.
   * @return  The http get url.
   */
  String httpAction() default "";

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

}

