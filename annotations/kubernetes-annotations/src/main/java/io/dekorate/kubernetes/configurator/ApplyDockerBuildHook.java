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
package io.dekorate.kubernetes.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.KubernetesConfigFluent;
import io.dekorate.doc.Description;

@Description("Apply the docker build hook configuration.")
public class ApplyDockerBuildHook extends Configurator<KubernetesConfigFluent> {

  private static final String Dekorate_BUILD = "dekorate.build";
  private static final String Dekorate_PUSH = "dekorate.push";
  private static final String Dekorate_DOCKER_REGISTRY = "dekorate.docker.registry";

  @Override
  public void visit(KubernetesConfigFluent config) {
    config.withAutoBuildEnabled(Boolean.parseBoolean(System.getProperty(Dekorate_BUILD, String.valueOf(config.isAutoBuildEnabled()))))
      .withAutoPushEnabled(Boolean.parseBoolean(System.getProperty(Dekorate_PUSH, String.valueOf(config.isAutoPushEnabled()))))
      .withRegistry(System.getProperty(Dekorate_DOCKER_REGISTRY, String.valueOf(config.getRegistry())));
  }
}
