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

package io.dekorate.component.model;

import io.dekorate.deps.kubernetes.api.KubernetesResourceMappingProvider;
import io.dekorate.deps.kubernetes.api.model.KubernetesResource;

import java.util.HashMap;
import java.util.Map;


public class ComponentMappingProvider implements KubernetesResourceMappingProvider {

  private Map<String, Class<? extends KubernetesResource>> mappings = new HashMap<String, Class<? extends KubernetesResource>>() {{
      put("devexp.runtime.redhat.com/v1alpha2#Component", Component.class);
    put("devexp.runtime.redhat.com/v1alpha2#Link", Link.class);
    put("devexp.runtime.redhat.com/v1alpha2#Capability", Capability.class);
    }};

  public Map<String, Class<? extends KubernetesResource>> getMappings() {
    return mappings;
  }
}
