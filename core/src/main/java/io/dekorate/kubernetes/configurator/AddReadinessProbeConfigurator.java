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

package io.dekorate.kubernetes.configurator;

import io.dekorate.kubernetes.config.BaseConfigFluent;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Probe;
import io.dekorate.utils.Beans;

public class AddReadinessProbeConfigurator extends Configurator<BaseConfigFluent> {

  private final Probe probe;
  private final boolean overwrite;

	public AddReadinessProbeConfigurator(Probe probe, boolean overwrite) {
    this.probe = probe;
    this.overwrite = overwrite;
	}

	@Override
	public void visit(BaseConfigFluent config) {
    Probe existing = config.getReadinessProbe();
    config.withReadinessProbe(overwrite ?
                              Beans.combine(existing, probe) :
                              Beans.combine(probe, existing));
	}
}
