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
package io.ap4k.decorator;

import io.ap4k.config.Probe;
import io.ap4k.utils.Strings;
import io.ap4k.deps.kubernetes.api.model.ContainerBuilder;
import io.ap4k.doc.Description;

@Description("Add a readiness probe to all containers.")
public class AddReadinessProbe extends AbstractAddProbe {

  public AddReadinessProbe(Probe probe) {
    super(probe);
  }

  @Override
  public void visit(ContainerBuilder container) {
    if (probe == null) {
      return;
    }
    if (Strings.isNullOrEmpty(probe.getExecAction()) &&
        Strings.isNullOrEmpty(probe.getHttpAction()) &&
        Strings.isNullOrEmpty(probe.getTcpSocketAction())) {
      return;
    }
    container.withNewReadinessProbe()
      .withExec(execAction(probe))
      .withHttpGet(httpGetAction(probe))
      .withTcpSocket(tcpSocketAction(probe))
      .withInitialDelaySeconds(probe.getInitialDelaySeconds())
      .withPeriodSeconds(probe.getPeriodSeconds())
      .withTimeoutSeconds(probe.getTimeoutSeconds())
      .endReadinessProbe();
  }

}
