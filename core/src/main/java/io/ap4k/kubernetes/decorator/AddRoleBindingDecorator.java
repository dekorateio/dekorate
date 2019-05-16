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
package io.ap4k.kubernetes.decorator;

import io.ap4k.Resources;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.deps.kubernetes.api.model.rbac.KubernetesRoleBindingBuilder;

/**
 * AddRoleBindingDecorator
 */
public class AddRoleBindingDecorator extends Decorator<KubernetesListBuilder> {

  private final Resources resources;
  private final String role;
   
  public AddRoleBindingDecorator(Resources resources, String role) {
    this.resources = resources;
    this.role = role;
  }


  public void visit(KubernetesListBuilder list) {
    list.addToItems(new KubernetesRoleBindingBuilder()
      .withNewMetadata()
      .withName(resources.getName()+":view")
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewRoleRef()
      .withName(role)
      .endRoleRef()
      .addNewSubject()
      .withKind("ServiceAccount")
      .withName(resources.getName())
      .endSubject());
  }
}

