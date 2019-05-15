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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.ap4k.deps.kubernetes.api.model.IntOrString;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.deps.kubernetes.api.model.ServicePort;
import io.ap4k.deps.kubernetes.api.model.ServicePortBuilder;
import io.ap4k.kubernetes.config.BaseConfig;
import io.ap4k.utils.Labels;

/**
 * AddRoleBindingDecorator
 */
public class AddRoleBindingDecorator extends Decorator<KubernetesListBuilder> {

  private final BaseConfig config;
  private final String role;
  private final Map<String, String> allLabels; //A combination of config and project labels.

  public AddRoleBindingDecorator(BaseConfig config, String role, Map<String, String> allLabels) {
    this.config = config;
    this.role = role;
    this.allLabels = allLabels;
  }

  public void visit(KubernetesListBuilder list) {
    list.addNewRoleBindingItem()
      .withNewMetadata()
      .withName(config.getName()+":view")
      .withLabels(allLabels)
      .endMetadata()
      .withNewRoleRef()
      .withName(role)
      .endRoleRef()
      .addNewSubject()
      .withKind("ServiceAccount")
      .withName(config.getName())
      .endSubject()
      .endRoleBindingItem();
  }
}

