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

import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;

public abstract class ResourceProvidingDecorator<T> extends Decorator<T> {

  private static final List<String> DEPLOYMENT_KINDS = Arrays.asList("Deployment", "DeploymentConfig", "Service", "Pipeline");

  public Optional<ObjectMeta> getDeploymentMetadata(KubernetesListBuilder list) {
    return list.getItems()
      .stream()
      .filter(h -> DEPLOYMENT_KINDS.contains(h.getKind()))
      .map(HasMetadata::getMetadata)
      .findFirst();
  }

  public ObjectMeta getMandatoryDeploymentMetadata(KubernetesListBuilder list) {
    return getDeploymentMetadata(list).orElseThrow(() -> new IllegalStateException("Expected at least one of: "+DEPLOYMENT_KINDS.stream().collect(Collectors.joining(","))+" to be present."));
  }
}
