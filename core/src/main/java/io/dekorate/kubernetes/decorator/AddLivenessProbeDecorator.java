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
package io.dekorate.kubernetes.decorator;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Probe;
import io.fabric8.kubernetes.api.model.ContainerFluent;

@Description("Add a liveness probe to all containers.")
public class AddLivenessProbeDecorator extends AbstractAddProbeDecorator {

  private final Logger LOGGER = LoggerFactory.getLogger();

  public AddLivenessProbeDecorator(String containerName, Probe probe) {
    super(containerName, probe);
  }

  public AddLivenessProbeDecorator(String deploymentName, String containerName, Probe probe) {
    super(deploymentName, containerName, probe);
  }

  @Override
  protected void doCreateProbe(ContainerFluent<?> container, Actions actions) {
    if (probe.getSuccessThreshold() != null && probe.getSuccessThreshold() != 1) {
      LOGGER.warning("Invalid success threshold value for liveness probe. It must be 1, found: "
          + probe.getSuccessThreshold() + "! The correct value of 1 will be forced!");
    }
    container.withNewLivenessProbe()
        .withExec(actions.execAction)
        .withHttpGet(actions.httpGetAction)
        .withTcpSocket(actions.tcpSocketAction)
        .withGrpc(actions.grpcAction)
        .withInitialDelaySeconds(probe.getInitialDelaySeconds())
        .withPeriodSeconds(probe.getPeriodSeconds())
        .withTimeoutSeconds(probe.getTimeoutSeconds())
        .withSuccessThreshold(1)
        .withFailureThreshold(probe.getFailureThreshold())
        .endLivenessProbe();
  }

}
