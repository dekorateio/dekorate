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

package io.dekorate.testing.openshift;

import io.dekorate.testing.AbstractDiagonsticsService;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;

public class DeploymentConfigDiagnostics extends AbstractDiagonsticsService<DeploymentConfig> {

  private static final String DEPLOYMENT_STATUS_HEADER_FORMAT = "Deployment: %s [%d/%d]";
  private static final String DEPLOYMENT_STATUS_CONDITIONS_FORMAT = "\t%-20s %-10s %-40s";

  public DeploymentConfigDiagnostics(KubernetesClient client) {
    super(client);
  }

  @Override
  public void display(DeploymentConfig deployment) {
    LOGGER.info("Diagnostics for kind: [DeploymentConfig] with name : [" + deployment.getMetadata().getName() + "].");
    displayStatus(deployment);
    displayEvents(deployment);
  }

  @Override
  public void displayStatus(DeploymentConfig deployment) {
    String name = deployment.getMetadata().getName();
    DeploymentConfig updated = getKubernetesClient().adapt(OpenShiftClient.class).deploymentConfigs().withName(name).get();

    if (updated != null) {
      int readyReplicas = updated.getStatus() != null && updated.getStatus().getReadyReplicas() != null
          ? updated.getStatus().getReadyReplicas()
          : 0;
      LOGGER.info(String.format(DEPLOYMENT_STATUS_HEADER_FORMAT, name, readyReplicas, updated.getSpec().getReplicas()));
      if (updated.getStatus() != null) {
        LOGGER.info(String.format(DEPLOYMENT_STATUS_CONDITIONS_FORMAT, "Type", "Status", "Message"));
        updated.getStatus().getConditions().forEach(c -> {
          LOGGER.info(String.format(DEPLOYMENT_STATUS_CONDITIONS_FORMAT,
              c.getType(),
              c.getStatus(),
              c.getMessage()));
        });
      } else {
        LOGGER.error("Failed to retrieve Deployment: [" + name + "]");
      }
    }
  }
}
