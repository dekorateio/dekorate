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
package io.ap4k.docker.configurator;

import io.ap4k.config.Configurator;
import io.ap4k.docker.config.DockerBuildConfigFluent;

public class ApplyHookConfig extends Configurator<DockerBuildConfigFluent> {

  private static final String AP4K_BUILD = "ap4k.build";

  @Override
  public void visit(DockerBuildConfigFluent config) {
    config.withAutoBuildEnabled(Boolean.parseBoolean(System.getProperty(AP4K_BUILD, String.valueOf(config.isAutoBuildEnabled()))));
  }
}
