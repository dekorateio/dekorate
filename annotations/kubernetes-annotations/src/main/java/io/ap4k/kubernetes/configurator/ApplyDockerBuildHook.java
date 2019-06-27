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
package io.ap4k.kubernetes.configurator;

import io.ap4k.kubernetes.config.Configurator;
import io.ap4k.kubernetes.config.KubernetesConfigFluent;
import io.ap4k.doc.Description;

@Description("Apply the docker build hook configuration.")
public class ApplyDockerBuildHook extends Configurator<KubernetesConfigFluent> {

  private static final String AP4K_BUILD = "ap4k.build";
  private static final String AP4K_PUSH = "ap4k.push";
  private static final String AP4K_DOCKER_REGISTRY = "ap4k.docker.registry";

  @Override
  public void visit(KubernetesConfigFluent config) {
    config.withAutoBuildEnabled(Boolean.parseBoolean(System.getProperty(AP4K_BUILD, String.valueOf(config.isAutoBuildEnabled()))))
      .withAutoPushEnabled(Boolean.parseBoolean(System.getProperty(AP4K_PUSH, String.valueOf(config.isAutoPushEnabled()))))
      .withRegistry(System.getProperty(AP4K_DOCKER_REGISTRY, String.valueOf(config.getRegistry())));
  }
}
