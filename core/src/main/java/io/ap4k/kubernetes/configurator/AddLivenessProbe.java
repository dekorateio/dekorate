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

package io.ap4k.kubernetes.configurator;

import io.ap4k.doc.Description;
import io.ap4k.kubernetes.config.Configurator;
import io.ap4k.kubernetes.config.BaseConfigFluent;
import io.ap4k.kubernetes.config.Probe;
import io.ap4k.kubernetes.config.ProbeBuilder;

@Description("sets the liveness port to all containers.")
public class AddLivenessProbe extends Configurator<BaseConfigFluent> {

  private final int port;

  public AddLivenessProbe(int port) {
    this.port = port;
  }

  @Override
  public void visit(BaseConfigFluent config) {
    Probe probe = new ProbeBuilder()
      .withHttpAction("http://ignored:"+port)
      .withInitialDelaySeconds(5)
      .withPeriodSeconds(10)
      .build();

    config.withLivenessProbe(probe);
  }
}
