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

package io.dekorate.kubernetes.decorator;

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

public class AddConfigMapResourceProvidingDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final String name;
  private final String namespace;

  public AddConfigMapResourceProvidingDecorator(String name, String namespace) {
    this.name = name;
    this.namespace = namespace;
  }

  @Override
  public void visit(KubernetesListBuilder list) {
    if (contains(list, "v1", "ConfigMap", name)) {
      return;
    }

    list.addNewConfigMapItem()
        .withNewMetadata()
        .withName(name)
        .withNamespace(namespace)
        .endMetadata()
        .endConfigMapItem();
  }
}
