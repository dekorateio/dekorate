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
package io.dekorate.micronaut;

import io.dekorate.WithSession;
import io.dekorate.Generator;
import io.dekorate.kubernetes.config.Port;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.kubernetes.configurator.AddPort;

import java.util.Collections;
import java.util.Map;

public interface MicronautWebAnnotationGenerator extends Generator, WithSession {

  Map WEB_ANNOTATIONS=Collections.emptyMap();

  @Override
  default void add(Map map) {
    Port port = detectHttpPort();
    session.configurators().add(new AddPort(port));
  }

  default Port detectHttpPort()  {
    return new PortBuilder().withContainerPort(8080).withName("http").build();
  }

}
