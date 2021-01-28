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
import java.util.Optional;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

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

  public Diagnostics(KubernetesClient client, Pods pods) {
    this.client = client;
    this.pods = pods;
  }

  public void displayAll() {
    PodList pods = client.pods().list();
    pods.getItems().stream().forEach(p -> display(p));
  }

  public <T extends HasMetadata> void display(T resource) {
    Optional<DiagnosticsService<T>> service = DiagnosticsFactory.create(client, (Class<T>) resource.getClass());
    service.ifPresent(s -> {
        try {
          s.display(resource);
        } catch (Exception e) {
          logger.error("Error displaying diagnostics for resource:" + resource.getKind() + " " + resource.getMetadata().getName());
          e.printStackTrace();
        }
     });
  }

  protected void log(Pod pod, Container container) {
    try {
      logger.info("Logs of pod: [" + pod.getMetadata().getName() + "], container: [" + container.getName() + "]");
      logger.info(client.pods().inNamespace(pod.getMetadata().getNamespace()).withName(pod.getMetadata().getName())
          .inContainer(container.getName()).tailingLines(100).withPrettyOutput().getLog());
    } catch (Throwable t) {
      logger.error("Failed to read logs, due to:" + t.getMessage());
    } finally {
      logger.info("---");
    }
  }

  protected void events(Pod pod) {
    try {
      Map<String, String> fields = new HashMap<>();
      fields.put("involvedObject.uid", pod.getMetadata().getUid());
      fields.put("involvedObject.name", pod.getMetadata().getName());
      fields.put("involvedObject.namespace", pod.getMetadata().getNamespace());

      EventList eventList = client.v1().events().inNamespace(pod.getMetadata().getNamespace()).withFields(fields).list();
      if (eventList == null) {
        return;
      }
      logger.info("Events of pod: [" + pod.getMetadata().getName() + "]");
      for (Event event : eventList.getItems()) {
        logger.info(String.format("%s\t\t%s", event.getReason(), event.getMessage()));
      }
    } catch (Throwable t) {
      logger.error("Failed to read events, due to:" + t.getMessage());
    } finally {
      logger.info("\t---");
    }
  }
}
