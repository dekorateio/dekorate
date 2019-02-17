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
package io.ap4k.testing.servicecatalog;

import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.deps.servicecatalog.api.client.ServiceCatalogClient;
import io.ap4k.deps.servicecatalog.api.model.ClusterServiceBrokerList;
import io.ap4k.testing.WithKubernetesClient;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.stream.Collectors;

public class ServiceCatalogExtension implements ExecutionCondition, WithKubernetesClient {

  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    try {
      KubernetesClient client = getKubernetesClient(context);
      if (!client.isAdaptable(ServiceCatalogClient.class)) {
        return ConditionEvaluationResult.disabled("Could not detect openshift.");
      }
      ClusterServiceBrokerList brokerList = getKubernetesClient(context).adapt(ServiceCatalogClient.class).clusterServiceBrokers().list();
      if (brokerList == null || brokerList.getItems() == null || brokerList.getItems().isEmpty()) {
        return ConditionEvaluationResult.disabled("Could not detect any broker.");
      }
      return ConditionEvaluationResult.enabled("Found brokers:" + brokerList.getItems().stream().map(b -> b.getMetadata().getName()).collect(Collectors.joining(" ," , "[", "]")));
    } catch (Throwable t) {
      return ConditionEvaluationResult.disabled("Could not communicate with Service Catalog API server.");
    }
  }
}
