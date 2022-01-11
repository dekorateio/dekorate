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

package io.dekorate.helm.manifest;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.ManifestGenerator;
import io.dekorate.ResourceRegistry;
import io.dekorate.WithProject;
import io.dekorate.helm.config.EditableHelmChartConfig;
import io.dekorate.helm.config.HelmChartConfig;
import io.dekorate.kubernetes.config.Configuration;

public class HelmManifestGenerator implements ManifestGenerator<HelmChartConfig>, WithProject {

  private static final String HELM = "helm";

  private final Logger LOGGER = LoggerFactory.getLogger();
  private final ResourceRegistry resourceRegistry;
  private final ConfigurationRegistry configurationRegistry;

  public HelmManifestGenerator(ResourceRegistry resourceRegistry, ConfigurationRegistry configurationRegistry) {
    this.resourceRegistry = resourceRegistry;
    this.configurationRegistry = configurationRegistry;
  }

  @Override
  public int order() {
    return 1301;
  }

  @Override
  public String getKey() {
    return HELM;
  }

  public boolean accepts(Class<? extends Configuration> type) {
    return type.equals(HelmChartConfig.class) || type.equals(EditableHelmChartConfig.class);
  }

  public void generate(HelmChartConfig config) {
    if (config.isEnabled()) {
      LOGGER.info("Processing helm configuration.");
      // TODO
    } else {
      // TODO
    }
  }
}
