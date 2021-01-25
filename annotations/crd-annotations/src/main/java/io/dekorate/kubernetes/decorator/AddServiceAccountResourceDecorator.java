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
package io.dekorate.kubernetes.decorator;

import io.dekorate.doc.Description;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Description("Add a ServiceAccount resource to the list of generated resources.")
public class AddServiceAccountResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final String name;

  public AddServiceAccountResourceDecorator() {
    this(null);
  }

  public AddServiceAccountResourceDecorator(String name) {
    this.name = name;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);
    String name = Strings.isNotNullOrEmpty(this.name) ? this.name : meta.getName();

    if (contains(list, "v1", "ServiceAccount", name)) {
      return;
    }

    list.addNewServiceAccountItem()
        .withNewMetadata()
        .withName(name)
        .withLabels(meta.getLabels())
        .endMetadata()
        .endServiceAccountItem();
  }
}
