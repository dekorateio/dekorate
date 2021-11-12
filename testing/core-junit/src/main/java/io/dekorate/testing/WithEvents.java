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
 * 
 * 
 * 
**/

package io.dekorate.testing;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.HasMetadata;

public interface WithEvents extends WithKubernetesClient {

  default EventList getEvents(ExtensionContext context, HasMetadata resource) {
    Map<String, String> fields = new HashMap<>();
    if (Strings.isNotNullOrEmpty(resource.getMetadata().getUid())) {
      fields.put("involvedObject.uid", resource.getMetadata().getUid());
    }
    if (Strings.isNotNullOrEmpty(resource.getMetadata().getNamespace())) {
      fields.put("involvedObject.namespace", resource.getMetadata().getNamespace());
    }
    fields.put("involvedObject.name", resource.getMetadata().getName());

    return getKubernetesClient(context).v1().events().inNamespace(resource.getMetadata().getNamespace()).withFields(fields)
        .list();
  }
}
