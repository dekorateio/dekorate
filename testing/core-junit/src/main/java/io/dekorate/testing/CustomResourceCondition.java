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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.testing.annotation.OnCustomResourcePresentCondition;
import io.dekorate.utils.Pluralize;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.http.HttpClient;
import io.fabric8.kubernetes.client.http.HttpRequest;
import io.fabric8.kubernetes.client.http.HttpResponse;
import io.fabric8.kubernetes.client.utils.URLUtils;

public class CustomResourceCondition implements ExecutionCondition, WithKubernetesClient {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    Optional<OnCustomResourcePresentCondition> annotation = context.getElement()
        .map(e -> e.getAnnotation(OnCustomResourcePresentCondition.class));
    if (!annotation.isPresent()) {
      return ConditionEvaluationResult.enabled("Condition not found!");
    }
    OnCustomResourcePresentCondition condition = annotation.get();
    try {
      String apiVersion = condition.apiVersion();
      String kind = condition.kind();
      String plural = Strings.isNotNullOrEmpty(condition.plural()) ? condition.plural()
          : Pluralize.FUNCTION.apply(kind).toLowerCase();
      String name = condition.name();
      String namespace = condition.namespace();

      KubernetesClient client = getKubernetesClient(context);
      Config config = client.getConfiguration();

      HttpClient httpClient = client.getHttpClient();

      List<String> parts = new ArrayList<>();
      parts.add(config.getMasterUrl());
      parts.add("apis");
      parts.add(apiVersion);

      if (Strings.isNotNullOrEmpty(namespace)) {
        parts.add("namespaces");
        parts.add(namespace);
      }

      parts.add(plural);
      if (Strings.isNotNullOrEmpty(name)) {
        parts.add(name);
      }
      parts.add(plural);
      String requestUrl = URLUtils.join(parts.stream().toArray(s -> new String[s]));

      final HttpRequest request = httpClient.newHttpRequestBuilder()
          .uri(requestUrl)
          .build();
      HttpResponse<String> response = httpClient.sendAsync(request, String.class).get(10, TimeUnit.SECONDS);
      if (!response.isSuccessful()) {
        return ConditionEvaluationResult.disabled("Could not lookup custom resource.");
      }

      //TODO: Add support for cases where name() is empty. In this case the result will be a list.
      //We need to check if empty.
      return ConditionEvaluationResult.enabled("Found resource with apiVersion:" + apiVersion + " kind:" + kind
          + " namespace: " + (Strings.isNullOrEmpty(namespace) ? "any" : namespace) + " name: "
          + (Strings.isNullOrEmpty(name) ? "any" : name));

    } catch (Throwable t) {
      return ConditionEvaluationResult.disabled("Could not lookup for service.");
    }
  }

}
