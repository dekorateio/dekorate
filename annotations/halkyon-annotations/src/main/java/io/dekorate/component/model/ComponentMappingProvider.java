/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.dekorate.component.model;

import java.util.HashMap;
import java.util.Map;

import io.dekorate.deps.kubernetes.api.KubernetesResourceMappingProvider;
import io.dekorate.deps.kubernetes.api.model.KubernetesResource;


public class ComponentMappingProvider implements KubernetesResourceMappingProvider {
  
  private Map<String, Class<? extends KubernetesResource>> mappings = new HashMap<String, Class<? extends KubernetesResource>>() {{
    put("halkyon.io/v1beta1#Component", Component.class);
    put("halkyon.io/v1beta1#Link", Link.class);
    put("halkyon.io/v1beta1#Capability", Capability.class);
  }};
  
  public Map<String, Class<? extends KubernetesResource>> getMappings() {
    return mappings;
  }
}
