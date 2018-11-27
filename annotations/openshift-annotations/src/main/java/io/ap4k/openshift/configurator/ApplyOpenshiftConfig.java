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
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.S2iConfigFluent;
import io.ap4k.doc.Description;

@Description("Applies group, name and version from OpenshiftConfig to EnableS2iBuild config.")
public class ApplyOpenshiftConfig extends Configurator<S2iConfigFluent> {


  private final OpenshiftConfig openshiftConfig;

  public ApplyOpenshiftConfig(OpenshiftConfig openshiftConfig) {
    this.openshiftConfig = openshiftConfig;
  }

  @Override
  public void visit(S2iConfigFluent fluent) {
    fluent.withGroup(openshiftConfig.getGroup())
      .withName(openshiftConfig.getName())
      .withVersion(openshiftConfig.getVersion());

  }
}
