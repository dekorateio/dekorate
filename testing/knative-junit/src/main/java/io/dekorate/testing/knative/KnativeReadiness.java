/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dekorate.testing.knative;

import io.fabric8.knative.serving.v1.Service;
import io.fabric8.knative.serving.v1.ServiceSpec;
import io.fabric8.knative.serving.v1.ServiceStatus;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.kubernetes.client.utils.Utils;

public class KnativeReadiness {

  public static boolean isReady(HasMetadata item) {
    if (isReadiableKubernetesResource(item)) {
      return isKubernetesResourceReady(item);
    } else if (isReadiableKnativeResource(item)) {
      return isKnativeResourceReady(item);
    } else {
      throw new IllegalArgumentException(
          "Item needs to be one of [Node, Deployment, ReplicaSet, StatefulSet, Pod, ReplicationController], but was: ["
              + (item != null ? item.getKind() : "Unknown (null)") + "]");
    }
  }

  private static boolean isKubernetesResourceReady(HasMetadata item) {

    if (item instanceof Deployment) {
      return Readiness.isDeploymentReady((Deployment) item);
    } else if (item instanceof io.fabric8.kubernetes.api.model.extensions.Deployment) {
      return Readiness.isExtensionsDeploymentReady((io.fabric8.kubernetes.api.model.extensions.Deployment) item);
    } else if (item instanceof ReplicaSet) {
      return Readiness.isReplicaSetReady((ReplicaSet) item);
    } else if (item instanceof Pod) {
      return Readiness.isPodReady((Pod) item);
    } else if (item instanceof ReplicationController) {
      return Readiness.isReplicationControllerReady((ReplicationController) item);
    } else if (item instanceof Endpoints) {
      return Readiness.isEndpointsReady((Endpoints) item);
    } else if (item instanceof Node) {
      return Readiness.isNodeReady((Node) item);
    } else if (item instanceof StatefulSet) {
      return Readiness.isStatefulSetReady((StatefulSet) item);
    }
    return false;
  }

  private static boolean isKnativeResourceReady(HasMetadata item) {
    if (item instanceof Service) {
      return isServiceReady((Service) item);
    }
    return false;
  }

  protected static boolean isReadiableKubernetesResource(HasMetadata item) {
    return (item instanceof Deployment ||
        item instanceof io.fabric8.kubernetes.api.model.extensions.Deployment ||
        item instanceof ReplicaSet ||
        item instanceof Pod ||
        item instanceof ReplicationController ||
        item instanceof Endpoints ||
        item instanceof Node ||
        item instanceof StatefulSet);
  }

  protected static boolean isReadiableKnativeResource(HasMetadata item) {
    return item instanceof Service;
  }

  public static boolean isServiceReady(Service s) {
    Utils.checkNotNull(s, "Service can't be null.");
    ServiceSpec spec = s.getSpec();
    ServiceStatus status = s.getStatus();

    if (status.getConditions() != null && status.getConditions().stream().anyMatch(c -> "OK".equals(c.getStatus()))) {
      return true;
    }
    return false;
  }
}
