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
package io.ap4k.openshift.configurator;

import io.ap4k.kubernetes.config.Configurator;
import io.ap4k.openshift.config.S2iConfigFluent;
import io.ap4k.doc.Description;

@Description("Apply source to image build hook.")
public class ApplySourceToImageHook extends Configurator<S2iConfigFluent> {

  private static final String AP4K_DEPLOY = "ap4k.deploy";

  @Override
  public void visit(S2iConfigFluent config) {
    config.withAutoDeployEnabled(Boolean.parseBoolean(System.getProperty(AP4K_DEPLOY, String.valueOf(config.isAutoDeployEnabled()))));
  }
}
