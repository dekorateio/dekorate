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

import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.dekorate.doc.Description;

/**
 * AddRoleBindingDecorator
 */
@Description("Add a Rolebinding resource to the list of generated resources.")
public class AddRoleBindingResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

private static final String DEFAULT_RBAC_API_GROUP = "rbac.authorization.k8s.io";

  public static enum RoleKind {
    Role,
    ClusterRole
  }

  private final String roleName;
  private final RoleKind roleKind;

  public AddRoleBindingResourceDecorator(RoleKind roleKind, String roleName) {
    this.roleKind = roleKind;
    this.roleName = roleName;
  }

  public AddRoleBindingResourceDecorator(String roleName) {
    this(RoleKind.ClusterRole, roleName);
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);

    list.addToItems(new RoleBindingBuilder()
      .withNewMetadata()
      .withName(meta.getName()+":view")
      .withLabels(meta.getLabels())
      .endMetadata()
      .withNewRoleRef()
      .withKind(roleKind.name())
      .withName(roleName)
      .withApiGroup(DEFAULT_RBAC_API_GROUP)
      .endRoleRef()
      .addNewSubject()
      .withKind("ServiceAccount")
      .withName(meta.getName())
      .endSubject());
  }
}

