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

import static java.lang.Boolean.parseBoolean;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.ImageConfigurationFluent;

@Description("Apply build related info to image configuration.")
public class ApplyBuildToImageConfiguration extends Configurator<ImageConfigurationFluent> {

  private static final String DEKORATE_BUILD = "dekorate.build";
  private static final String DEKORATE_PUSH = "dekorate.push";

  @Override
  public void visit(ImageConfigurationFluent config) {
    config.withAutoBuildEnabled(parseBoolean(System.getProperty(DEKORATE_BUILD, String.valueOf(config.getAutoBuildEnabled()))))
        .withAutoPushEnabled(parseBoolean(System.getProperty(DEKORATE_PUSH, String.valueOf(config.getAutoPushEnabled()))));
  }
}
