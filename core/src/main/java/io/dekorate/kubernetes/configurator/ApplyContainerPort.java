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

package io.dekorate.kubernetes.configurator;

import java.util.Arrays;
import java.util.List;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.PortBuilder;
import io.dekorate.deps.kubernetes.api.builder.Predicate;

public class ApplyContainerPort extends Configurator<BaseConfigFluent<?>> {

  private static final String FALLBACK_PORT_NAME = "http";

  private final int containerPort;
  private final List<String> names;

  public ApplyContainerPort(int containerPort, List<String> names) {
    this.containerPort=containerPort;
    this.names=names;
  }
  public ApplyContainerPort(int containerPort, String... names) {
    this(containerPort, Arrays.asList(names));
  }

  @Override
  public void visit(BaseConfigFluent<?> config) {
    Predicate<PortBuilder> predicate = p -> names.contains(p.getName());
    if (config.hasMatchingPort(predicate)) {
      config.editMatchingPort(predicate).withContainerPort(containerPort).endPort();
    } else {
      String name = names.size() > 0 ? names.get(0) : FALLBACK_PORT_NAME;
      config.addNewPort()
        .withName(name)
        .withContainerPort(containerPort)
        .endPort();
    }
  }
}
