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

import java.util.Optional;

import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

public class KubernetesResources {

  public static KubernetesList loadGenerated(String group) {
    return Serialization.unmarshalAsList(
        Serialization.class.getClassLoader().getResourceAsStream("META-INF/dekorate/" + group + ".yml"));
  }

  /**
   * Find the first resource of the specified type.
   * 
   * @param list The list with the generated resources.
   * @param t The type of resource that we are looking for.
   * @return An optional containing the resource.
   */
  public static <T extends HasMetadata> Optional<T> findFirst(KubernetesList list, Class<T> t) {
    return (Optional<T>) list.getItems().stream().filter(i -> t.isInstance(i)).findFirst();
  }
}
