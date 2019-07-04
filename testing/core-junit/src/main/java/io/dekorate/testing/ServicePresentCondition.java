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
package io.dekorate.testing;

import io.dekorate.deps.kubernetes.api.model.Service;
import io.dekorate.deps.kubernetes.client.KubernetesClient;
import io.dekorate.testing.annotation.OnServicePresentCondition;
import io.dekorate.utils.Strings;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

public class ServicePresentCondition implements ExecutionCondition, WithKubernetesClient {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
   Optional<OnServicePresentCondition> annotation = context.getElement().map(e -> e.getAnnotation(OnServicePresentCondition.class));
   if (!annotation.isPresent()) {
    return ConditionEvaluationResult.enabled("Condition not found!");
   }
   OnServicePresentCondition condition = annotation.get();
    try {
      KubernetesClient client = getKubernetesClient(context);
      String namespace = Strings.isNotNullOrEmpty(condition.namespace()) ? condition.namespace() :  client.getNamespace();
      Service service = getKubernetesClient(context).services().inNamespace(namespace).withName(condition.value()).get();
      if (service != null) {
        return ConditionEvaluationResult.enabled("Found service:" + condition.value() + " in namespace:" + namespace + " .");
      } else {
        return ConditionEvaluationResult.disabled("Could not find service:" + condition.value() + " in namespace:" + namespace + " .");
      }
    } catch (Throwable t) {
      return ConditionEvaluationResult.disabled("Could not lookup for service.");
    }
  }

}
