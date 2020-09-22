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

public class SetSpringBootVersion extends Configurator<ConfigurationFluent<?>> {

  public static final ConfigKey<String> RUNTIME_VERSION = new ConfigKey<>("RUNTIME_VERSION", String.class);

  @Override
  public void visit(ConfigurationFluent<?> config) {
    config.addToAttributes(RUNTIME_VERSION, getSpringBootVersion());
  }

  public static String getSpringBootVersion() {
    try {
      return Class.forName("org.springframework.boot.autoconfigure.SpringBootApplication")
          .getPackage().getImplementationVersion();
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
