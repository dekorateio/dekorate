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

import java.util.HashMap;
import java.util.Map;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.deps.kubernetes.api.model.Container;
import io.dekorate.deps.kubernetes.api.model.Event;
import io.dekorate.deps.kubernetes.api.model.EventList;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.Pod;
import io.dekorate.deps.kubernetes.api.model.PodList;
import io.dekorate.deps.kubernetes.client.KubernetesClient;

/*
 * Originally implemented by iocanel in https://github.com/arquillian/arquillian-cube/blob/master/kubernetes/kubernetes/src/main/java/org/arquillian/cube/kubernetes/impl/feedback/DefaultFeedbackProvider.java
 */
public class Diagnostics {

  private Logger logger = LoggerFactory.getLogger();

  private final KubernetesClient client;
  private final Pods pods;

  public Diagnostics(KubernetesClient client) {
    this.client = client;
    this.pods = new Pods(client);
  }

  public <T extends HasMetadata> void display(T resource) {
    try {
      PodList podList = pods.list(resource);
      if (podList == null) {
        return;
      }

      for (Pod pod : podList.getItems()) {
        // That should only happen in tests.
        if (pod.getSpec() == null || pod.getSpec().getContainers() == null) {
          continue;
        }

        events(pod);

        for (Container container : pod.getSpec().getContainers()) {
          log(pod, container);
        }
      }
    } catch (Throwable t) {
      // ignore
    }
  }

  protected void log(Pod pod, Container container) {
    try {
      logger.warning("Logs of pod: [" + pod.getMetadata().getName() + "], container: ["
          + container.getName() + "]");
      logger.info(client.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName())
          .inContainer(container.getName()).tailingLines(100).withPrettyOutput().getLog());
    } catch (Throwable t) {
      logger.error("Failed to read logs, due to:" + t.getMessage());
    } finally {
      logger.warning("---");
    }
  }

  protected void events(Pod pod) {
    try {
      Map<String, String> fields = new HashMap<>();
      fields.put("involvedObject.uid", pod.getMetadata().getUid());
      fields.put("involvedObject.name", pod.getMetadata().getName());
      fields.put("involvedObject.namespace", pod.getMetadata().getNamespace());

      EventList eventList = client.events().inNamespace(pod.getMetadata().getNamespace()).withFields(fields).list();
      if (eventList == null) {
        return;
      }
      logger.warning("Events of pod: [" + pod.getMetadata().getName() + "]");
      for (Event event : eventList.getItems()) {
        logger.info(String.format("%s\t\t%s", event.getReason(), event.getMessage()));
      }
    } catch (Throwable t) {
      logger.error("Failed to read events, due to:" + t.getMessage());
    } finally {
      logger.warning("---");
    }
  }
}
