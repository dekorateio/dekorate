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
import io.dekorate.utils.Generics;
import io.dekorate.utils.Serialization;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface DiagnosticsService<T extends HasMetadata> {

  Logger LOGGER = LoggerFactory.getLogger();

  public KubernetesClient getKubernetesClient();

  default Class<T> getType() {
    return (Class) Generics.getTypeArguments(DiagnosticsService.class, getClass()).get(0);
  }

  public void display(T resource);

  public void displayStatus(T resource);

  default void displayResourceYaml(T resource) {
    try {
      LOGGER.info(Serialization.asYaml(resource));
    } finally {
      LOGGER.info("\t---");
    }
  }

  default void displayEvents(T resource) {
    try {
      Map<String, String> fields = new HashMap<>();
      if (Strings.isNotNullOrEmpty(resource.getMetadata().getUid())) {
        fields.put("involvedObject.uid", resource.getMetadata().getUid());
      }
      if (Strings.isNotNullOrEmpty(resource.getMetadata().getNamespace())) {
        fields.put("involvedObject.namespace", resource.getMetadata().getNamespace());
      }
      fields.put("involvedObject.name", resource.getMetadata().getName());

      EventList eventList = getKubernetesClient().v1().events().inNamespace(resource.getMetadata().getNamespace())
          .withFields(fields).list();
      if (eventList == null) {
        return;
      }
      LOGGER.info(String.format("Events of %s: [%s]", resource.getKind(), resource.getMetadata().getName()));
      for (Event event : eventList.getItems()) {
        LOGGER.info(String.format("%s\t\t%s", event.getReason(), event.getMessage()));
      }
    } catch (Throwable t) {
      LOGGER.error("Failed to read events, due to:" + t.getMessage());
    } finally {
      LOGGER.info("\t---");
    }
  }
}
