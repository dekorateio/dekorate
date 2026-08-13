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

import static io.dekorate.testing.Testing.DEKORATE_STORE;
import static io.dekorate.testing.Testing.KUBERNETES_LIST;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.project.Project;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.KubernetesClient;

public interface WithDiagnostics extends WithKubernetesClient, WithProject {

  String EXTENSION_ERROR = "EXTENSION_ERROR";
  String READINESS_FAILED = "READINESS_FAILED";

  default void extensionError(ExtensionContext context, String message) {
    context.getStore(DEKORATE_STORE).put(EXTENSION_ERROR, message);
  }

  default boolean hasExtensionError(ExtensionContext context) {
    return Strings.isNotNullOrEmpty(context.getStore(DEKORATE_STORE).getOrDefault(EXTENSION_ERROR, String.class, null));
  }

  default void readinessFailed(ExtensionContext context) {
    context.getStore(DEKORATE_STORE).put(READINESS_FAILED, true);
  }

  default boolean hasReadinessFailed(ExtensionContext context) {
    return context.getStore(DEKORATE_STORE).getOrDefault(READINESS_FAILED, boolean.class, false);
  }

  default boolean shouldDisplayDiagnostics(ExtensionContext context) {
    return context.getExecutionException().isPresent();
  }

  default void displayDiagnostics(ExtensionContext context) {
    KubernetesClient client = getKubernetesClient(context);
    final Diagnostics diagnostics = new Diagnostics(client);

    List<Project> projects = getProjects(context);
    for (Project project : projects) {
      String key = KUBERNETES_LIST + project.getRoot();
      KubernetesList resources = (KubernetesList) context.getStore(DEKORATE_STORE).get(key);
      if (resources != null) {
        resources.getItems().stream().forEach(r -> diagnostics.display(r));
      }
    }

    diagnostics.displayAll();
  }
}
