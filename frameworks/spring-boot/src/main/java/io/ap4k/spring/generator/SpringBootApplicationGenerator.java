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
package io.ap4k.spring.generator;

import io.ap4k.WithSession;
import io.ap4k.Generator;
import io.ap4k.spring.configurator.SetSpringBootRuntime;

import java.util.Collections;
import java.util.Map;

public interface SpringBootApplicationGenerator extends Generator, WithSession {

  Map SPRING_BOOT_APPLICATION=Collections.emptyMap();

  @Override
  default void add(Map map) {
     session.configurators().add(new SetSpringBootRuntime());
  }
}
