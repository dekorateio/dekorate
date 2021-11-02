/**
 * Copyright 2021 The original authors.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.ExtensionContext;

import io.dekorate.DekorateException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Waitable;

/**
 * Mixin for deploying / deleting additional resources.
 */
public interface WithAdditionalResources extends WithKubernetesClient {
  default void loadAdditionalResources(ExtensionContext context, String[] additionalResources,
      long additionalResourcesTimeout) {

    KubernetesClient client = getKubernetesClient(context);

    for (String additionalResource : additionalResources) {
      try (FileInputStream is = new FileInputStream(additionalResource)) {
        client.resourceList(client.load(is).get()).createOrReplace().stream()
            .filter(item -> item instanceof Waitable)
            .forEach(item -> ((Waitable) item).waitUntilReady(additionalResourcesTimeout, TimeUnit.MILLISECONDS));

      } catch (IOException e) {
        throw DekorateException.launderThrowable("Failed to load resource: " + additionalResource, e);
      }
    }
  }

  default void deleteAdditionalResources(ExtensionContext context, String[] additionalResources) {
    KubernetesClient client = getKubernetesClient(context);
    for (String additionalResource : additionalResources) {
      try (FileInputStream is = new FileInputStream(additionalResource)) {
        List<HasMetadata> items = client.load(is).get();
        items.forEach(r -> deleteResource(client, r));
      } catch (IOException ignored) {
      }
    }
  }

  void deleteResource(KubernetesClient client, HasMetadata resource);
}
