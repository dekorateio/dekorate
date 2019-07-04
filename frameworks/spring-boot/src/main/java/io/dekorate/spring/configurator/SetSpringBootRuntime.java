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
package io.dekorate.spring.configurator;

import io.dekorate.kubernetes.config.ConfigKey;
import io.dekorate.kubernetes.config.ConfigurationFluent;
import io.dekorate.kubernetes.config.Configurator;

public class SetSpringBootRuntime extends Configurator<ConfigurationFluent<?>> {

  // TODO : Make this property generic as it will also be ued by Vert.x, Tornthail, ...
  public static final ConfigKey<String> RUNTIME_TYPE = new ConfigKey<>("RUNTIME_TYPE", String.class);
  private static String RUNTIME_SPRING_BOOT = "spring-boot";

  @Override
  public void visit(ConfigurationFluent<?> config) {
    config.addToAttributes(RUNTIME_TYPE,RUNTIME_SPRING_BOOT);
  }
}
