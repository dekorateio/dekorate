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

import java.util.Collections;

import io.dekorate.doc.Description;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;

/**
 * AddRoleBindingDecorator
 */
@Description("Add a Rolebinding resource to the list of generated resources.")
public class AddRoleBindingResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private static final String DEFAULT_RBAC_API_GROUP = "rbac.authorization.k8s.io";

  public static enum RoleKind {
    Role, ClusterRole
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
    // If name is null, it will get the first deployment resource found.
    ObjectMeta meta = getMandatoryDeploymentMetadata(list, name);
    String actualName = Strings.isNotNullOrEmpty(name) ? name : meta.getName();
    String roleBindingName = actualName + "-" + this.role;
    String serviceAccount = Strings.isNotNullOrEmpty(this.serviceAccount) ? this.serviceAccount : actualName;

    if (contains(list, "rbac.authorization.k8s.io/v1", "RoleBinding", roleBindingName)) {
      return;
    }

    list.addToItems(new RoleBindingBuilder()
        .withNewMetadata()
        .withName(roleBindingName)
        .withLabels(Strings.isNotNullOrEmpty(name) ? meta.getLabels() : Collections.emptyMap())
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
