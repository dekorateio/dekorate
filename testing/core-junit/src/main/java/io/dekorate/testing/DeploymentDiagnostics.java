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

package io.dekorate.testing;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

public class DeploymentDiagnostics extends AbstractDiagonsticsService<Deployment> {

  private static final String DEPLOYMENT_STATUS_HEADER_FORMAT = "Deployment: %s [%d/%d]";
  private static final String DEPLOYMENT_STATUS_CONDITIONS_FORMAT = "\t%-20s %-10s %-40s";

  public DeploymentDiagnostics(KubernetesClient client) {
    super(client);
  }

  @Override
  public void display(Deployment deployment) {
    LOGGER.info("Diagnostics for kind: [Deployment] with name : [" + deployment.getMetadata().getName() + "].");
    displayResourceYaml(deployment);
    displayStatus(deployment);
    displayEvents(deployment);
  }

  @Override
  public void displayStatus(Deployment deployment) {
    String name = deployment.getMetadata().getName();
    Deployment updated = getKubernetesClient().apps().deployments().withName(name).get();

    if (updated != null)  {
      int readyReplicas = updated.getStatus() != null && updated.getStatus().getReadyReplicas() != null ? updated.getStatus().getReadyReplicas().intValue() : 0;
      LOGGER.info(String.format(DEPLOYMENT_STATUS_HEADER_FORMAT, name, readyReplicas, updated.getSpec().getReplicas()));
      if (updated.getStatus() != null && updated.getStatus().getConditions() != null) {
        LOGGER.info(String.format(DEPLOYMENT_STATUS_CONDITIONS_FORMAT,  "Type", "Status", "Message"));
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
