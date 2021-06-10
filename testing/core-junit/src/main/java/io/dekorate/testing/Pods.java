/**
 * Copyright 2018 The original authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy map the License at
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

import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;

public class Pods {

  private final KubernetesClient client;

  public Pods(KubernetesClient client) {
    this.client = client;
  }

  /**
   * Finds the pod that correspond to the specified resource.
   * 
   * @param resource The resource.
   * @return The podList with the matching pods.
   */
  public PodList list(Object resource) {
    if (resource instanceof KubernetesList) {
      KubernetesList list = (KubernetesList) resource;
      return new PodListBuilder()
          .withItems(list.getItems().stream().map(i -> list(i)).filter(i -> i != null && !i.getItems().isEmpty())
              .flatMap(i -> i.getItems().stream()).collect(Collectors.toList()))
          .build();
    }
    if (resource instanceof Pod) {
      return new PodListBuilder().withItems((Pod) resource).build();
    } else if (resource instanceof Endpoints) {
      return list(client.services().inNamespace(((Endpoints) resource).getMetadata().getNamespace())
          .withName(((Endpoints) resource).getMetadata().getName()).get());
    } else if (resource instanceof Service) {
      return client.pods().inNamespace(((Service) resource).getMetadata().getNamespace())
          .withLabels(((Service) resource).getSpec().getSelector()).list();
    } else if (resource instanceof ReplicationController) {
      return client.pods().inNamespace(((ReplicationController) resource).getMetadata().getNamespace())
          .withLabels(((ReplicationController) resource).getSpec().getSelector()).list();
    } else if (resource instanceof ReplicaSet) {
      return map((ReplicaSet) resource);
    } else if (resource instanceof Deployment) {
      return map((Deployment) resource);
    } else {
      return new PodListBuilder().build();
    }
  }

  /**
   * Returns the {@link PodList} that match the specified {@link Deployment}.
   * 
   * @param deployment The {@link Deployment}
   */
  protected PodList map(Deployment deployment) {
    FilterWatchListDeletable<Pod, PodList> podLister = client.pods()
        .inNamespace(deployment.getMetadata().getNamespace());
    if (deployment.getSpec().getSelector().getMatchLabels() != null) {
      podLister.withLabels(deployment.getSpec().getSelector().getMatchLabels());
    }
    if (deployment.getSpec().getSelector().getMatchExpressions() != null) {
      for (LabelSelectorRequirement req : deployment.getSpec().getSelector().getMatchExpressions()) {
        switch (req.getOperator()) {
          case "In":
            podLister.withLabelIn(req.getKey(), req.getValues().toArray(new String[] {}));
            break;
          case "NotIn":
            podLister.withLabelNotIn(req.getKey(), req.getValues().toArray(new String[] {}));
            break;
          case "DoesNotExist":
            podLister.withoutLabel(req.getKey());
            break;
          case "Exists":
            podLister.withLabel(req.getKey());
            break;
        }
      }
    }
    return podLister.list();
  }

  /**
   * Returns the {@link PodList} that match the specified {@link ReplicaSet}.
   *
   * @param replicaSet The {@link ReplicaSet}
   */
  protected PodList map(ReplicaSet replicaSet) {
    FilterWatchListDeletable<Pod, PodList> podLister = client.pods()
        .inNamespace(replicaSet.getMetadata().getNamespace());
    if (replicaSet.getSpec().getSelector().getMatchLabels() != null) {
      podLister.withLabels(replicaSet.getSpec().getSelector().getMatchLabels());
    }
    if (replicaSet.getSpec().getSelector().getMatchExpressions() != null) {
      for (LabelSelectorRequirement req : replicaSet.getSpec().getSelector().getMatchExpressions()) {
        switch (req.getOperator()) {
          case "In":
            podLister.withLabelIn(req.getKey(), req.getValues().toArray(new String[] {}));
            break;
          case "NotIn":
            podLister.withLabelNotIn(req.getKey(), req.getValues().toArray(new String[] {}));
            break;
          case "DoesNotExist":
            podLister.withoutLabel(req.getKey());
            break;
          case "Exists":
            podLister.withLabel(req.getKey());
            break;
        }
      }
    }
    return podLister.list();
  }
}
