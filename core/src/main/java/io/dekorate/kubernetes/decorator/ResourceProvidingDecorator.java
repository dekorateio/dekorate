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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public abstract class ResourceProvidingDecorator<T> extends Decorator<T> {

  private static final List<String> DEPLOYMENT_KINDS = Arrays.asList("Deployment", "DeploymentConfig", "Service", "StatefulSet",
      "Pipeline", "Task", "Job", "CronJob");

  protected static final String ANY = null;

  public boolean contains(KubernetesListBuilder list, String apiVersion, String kind, String name) {
    return list.buildItems().stream().filter(i -> match(i, apiVersion, kind, name)).findAny().isPresent();
  }

  public boolean match(HasMetadata h, String apiVersion, String kind, String name) {
    if (Strings.isNotNullOrEmpty(apiVersion) && !apiVersion.equals(h.getApiVersion())) {
      return false;
    }
    if (Strings.isNotNullOrEmpty(kind) && !kind.endsWith(h.getKind())) {
      return false;
    }
    if (Strings.isNotNullOrEmpty(name) && !name.equals(h.getMetadata().getName())) {
      return false;
    }
    return true;
  }

  public List<ObjectMeta> getDeploymentMetadataList(KubernetesListBuilder list) {
    return getDeploymentMetadataList(list, ANY);
  }

  public List<ObjectMeta> getDeploymentMetadataList(KubernetesListBuilder list, String name) {
    // In 99% of the cases we select metadata by name.
    // There are some edge cases (e.g. RoleBindings) where a suffix is added (e.g. <name>:deployer).
    // We need to get rid of such suffixes when present as we NEVER have thtem in `deployment` kinds.
    String trimedName = Strings.isNotNullOrEmpty(name) ? name.replaceAll("[:].*$", "") : name;

    return list.buildItems()
        .stream()
        .filter(h -> DEPLOYMENT_KINDS.contains(h.getKind()))
        .filter(h -> trimedName == ANY || h.getMetadata().getName().equals(trimedName))
        .map(HasMetadata::getMetadata)
        .collect(Collectors.toList());
  }

  public Optional<ObjectMeta> getDeploymentMetadata(KubernetesListBuilder list) {
    return getDeploymentMetadata(list, ANY);
  }

  public Optional<ObjectMeta> getDeploymentMetadata(KubernetesListBuilder list, String name) {
    return getDeploymentMetadataList(list, name).stream().findFirst();
  }

  public Optional<HasMetadata> getDeploymentHasMetadata(KubernetesListBuilder list) {
    return getDeploymentHasMetadata(list, ANY);
  }

  public Optional<HasMetadata> getDeploymentHasMetadata(KubernetesListBuilder list, String name) {
    return list.buildItems().stream().filter(h -> DEPLOYMENT_KINDS.contains(h.getKind())).findFirst();
  }

  public ObjectMeta getMandatoryDeploymentMetadata(KubernetesListBuilder list) {
    return getMandatoryDeploymentMetadata(list, ANY);
  }

  public ObjectMeta getMandatoryDeploymentMetadata(KubernetesListBuilder list, String name) {
    return getDeploymentMetadata(list, name).orElseThrow(() -> new IllegalStateException(
        "Expected at least one of: " + DEPLOYMENT_KINDS.stream().collect(Collectors.joining(","))
            + (Strings.isNotNullOrEmpty(name) ? " with name:" + name : "")
            + " to be present."));
  }

  public HasMetadata getMandatoryDeploymentHasMetadata(KubernetesListBuilder list) {
    return getMandatoryDeploymentHasMetadata(list, ANY);
  }

  public HasMetadata getMandatoryDeploymentHasMetadata(KubernetesListBuilder list, String name) {
    return getDeploymentHasMetadata(list, name).orElseThrow(() -> new IllegalStateException(
        "Expected at least one of: " + DEPLOYMENT_KINDS.stream().collect(Collectors.joining(","))
            + (Strings.isNotNullOrEmpty(name) ? " with name:" + name : "")
            + " to be present."));
  }
}
