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

import io.dekorate.Resources;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.doc.Description;

@Description("Add a ServiceAccount resource to the list of generated resources.")
public class AddServiceAccountDecorator extends Decorator<KubernetesListBuilder> {

  private final Resources resources;
   
  public AddServiceAccountDecorator(Resources resources) {
    this.resources = resources;
  }

  public void visit(KubernetesListBuilder list) {
    list.addNewServiceAccountItem()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .endServiceAccountItem();
  }
}

