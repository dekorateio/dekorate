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
package io.dekorate.openshift.configurator;

import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.openshift.config.OpenshiftConfig;
import io.dekorate.openshift.config.OpenshiftConfigFluent;
import io.dekorate.doc.Description;

@Description("Apply source to image build hook.")
public class ApplySourceToImageHook extends Configurator<OpenshiftConfigFluent> {

  private static final String Dekorate_BUILD = "dekorate.build";
  private static final String Dekorate_DEPLOY = "dekorate.deploy";

  private final OpenshiftConfig openshiftConfig;

  public ApplySourceToImageHook(OpenshiftConfig openshiftConfig) {
    this.openshiftConfig = openshiftConfig;
  }

  @Override
  public void visit(OpenshiftConfigFluent config) {
    config
      .withAutoBuildEnabled(Boolean.parseBoolean(System.getProperty(Dekorate_BUILD, String.valueOf(config.isAutoBuildEnabled()))))
      .withAutoDeployEnabled(Boolean.parseBoolean(System.getProperty(Dekorate_DEPLOY, String.valueOf(config.isAutoDeployEnabled() || openshiftConfig.isAutoDeployEnabled()))));
  }
}
