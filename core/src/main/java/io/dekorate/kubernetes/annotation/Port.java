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

public @interface Port {

  /**
   * The container port name.
   * 
   * @return The name.
   */
  String name();

  /**
   * The port number. Refers to the container port.
   * 
   * @return The port number.
   */
  int containerPort();

  /**
   * The host port.
   * When a host port is not specified (or is set to 0) then the container port will be used.
   * 
   * @return The host port, or 0 if none specified.
   */
  int hostPort() default 0;

  /**
   * The port number. Refers to the container port.
   *
   * @return The port number.
   */
  int nodePort() default 0;

  /**
   * The application path (refers to web application path).
   * 
   * @return The path, defaults to /.
   */
  String path() default "/";

  Protocol protocol() default Protocol.TCP;
}
