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
package io.dekorate.project;

import io.dekorate.kubernetes.config.ApplicationConfigurationFluent;
import io.dekorate.kubernetes.config.ConfigurationFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.ImageConfigurationFluent;
import io.dekorate.utils.Strings;

public class ApplyProjectInfo extends Configurator<ConfigurationFluent> {

  private static final String APP_NAME = "app.name";
  private static final String APP_VERSION = "app.version";

  private final Project project;

  public ApplyProjectInfo(Project project) {
    this.project = project;
  }

  @Override
  public void visit(ConfigurationFluent fluent) {
    fluent.withProject(project);
    if (fluent instanceof ApplicationConfigurationFluent) {
      ApplicationConfigurationFluent appConfig = (ApplicationConfigurationFluent) fluent;
      appConfig
          .withName(System.getProperty(APP_NAME,
              Strings.isNotNullOrEmpty(appConfig.getName()) ? appConfig.getName()
                  : project.getBuildInfo().getName()))
          .withVersion(System.getProperty(APP_VERSION,
              Strings.isNotNullOrEmpty(appConfig.getVersion()) ? appConfig.getVersion()
                  : project.getBuildInfo().getVersion()));
    } else if (fluent instanceof ImageConfigurationFluent) {
      ImageConfigurationFluent imageConfig = (ImageConfigurationFluent) fluent;
      imageConfig
          .withName(System.getProperty(APP_NAME,
              Strings.isNotNullOrEmpty(imageConfig.getName()) ? imageConfig.getName()
                  : project.getBuildInfo().getName()))
          .withVersion(System.getProperty(APP_VERSION,
              Strings.isNotNullOrEmpty(imageConfig.getVersion()) ? imageConfig.getVersion()
                  : project.getBuildInfo().getVersion()));
    }
  }
}
