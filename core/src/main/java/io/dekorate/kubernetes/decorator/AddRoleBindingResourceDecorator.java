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
import io.dekorate.utils.Strings;

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

  private final String serviceAccount;
  private final String name;
  private final String role;
  private final RoleKind kind;


  public AddRoleBindingResourceDecorator(String role) {
    this(null, null, role, RoleKind.ClusterRole);
  }

  public AddRoleBindingResourceDecorator(String role, RoleKind kind) {
    this(null, null, role, kind);
  }

  public AddRoleBindingResourceDecorator(String name, String serviceAccount, String role, RoleKind kind) {
    this.name = name;
    this.serviceAccount = serviceAccount;
    this.role = role;
    this.kind = kind;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);
    String name = Strings.isNotNullOrEmpty(this.name) ? this.name :  meta.getName() + ":view";
    String serviceAccount = Strings.isNotNullOrEmpty(this.serviceAccount) ? this.serviceAccount :  meta.getName();

    if (contains(list, "rbac.authorization.k8s.io/v1", "RoleBinding", name)) {
      return;
    }

    list.addToItems(new RoleBindingBuilder()
      .withNewMetadata()
      .withName(name)
      .withLabels(meta.getLabels())
      .endMetadata()
      .withNewRoleRef()
      .withKind(kind.name())
      .withName(role)
      .withApiGroup(DEFAULT_RBAC_API_GROUP)
      .endRoleRef()
      .addNewSubject()
      .withKind("ServiceAccount")
      .withName(serviceAccount)
      .endSubject());
  }
}

