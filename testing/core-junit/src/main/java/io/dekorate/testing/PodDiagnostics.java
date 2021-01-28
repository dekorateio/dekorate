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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

public class PodDiagnostics extends AbstractDiagonsticsService<Pod> {

  private static final String CONTAINER_STATUS_FORMAT = "%-40s %-7s %-7s %s";

  public PodDiagnostics(KubernetesClient client) {
		super(client);
	}

   
  public void display(Pod pod) {
    LOGGER.info("Diagnostics for kind: [Pod] with name : [" + pod.getMetadata().getName() + "].");
    displayStatus(pod);
    displayEvents(pod);
    displayLogs(pod);
  }

@Override
	public void displayStatus(Pod pod) {
    LOGGER.info("Container statuses of Pod:" + pod.getMetadata().getName());
    if (pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null) {
      LOGGER.info(String.format(CONTAINER_STATUS_FORMAT, "Name", "Running", "Ready", "Image"));
      pod.getStatus().getContainerStatuses().forEach(c -> {
          LOGGER.info(String.format(CONTAINER_STATUS_FORMAT, c.getName(), c.getState().getRunning() != null, c.getReady(), c.getImage()));
        });
    } else {
      LOGGER.warning("No containers statuses found.");
    }
	}

	protected void displayLogs(Pod pod) {
    for (Container container : pod.getSpec().getContainers()) {
      displayLogs(pod, container);
    }
  }
  
  protected void displayLogs(Pod pod, Container container) {
    try {
      LOGGER.info("Logs of pod: [" + pod.getMetadata().getName() + "], container: [" + container.getName() + "]");
      LOGGER.info(getKubernetesClient().pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName())
          .inContainer(container.getName()).tailingLines(100).withPrettyOutput().getLog());
    } catch (Throwable t) {
      LOGGER.error("Failed to read logs, due to:" + t.getMessage());
    } finally {
      LOGGER.info("---");
    }
  }
}
