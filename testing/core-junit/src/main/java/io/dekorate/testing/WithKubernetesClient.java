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

import static io.dekorate.testing.Testing.DEKORATE_STORE;
import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.dekorate.DekorateException;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
 * Mixin for storing the kubernetes client into the context.
 * It also provides functionality for injecting the client.
 */
public interface WithKubernetesClient extends TestInstancePostProcessor {

  String KUBERNETES_CLIENT = "KUBERNETES_CLIENT";

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
        .forEach(f -> injectKubernetesClient(context, testInstance, f));
  }

  /**
   * Inject an instance of {@link KubernetesClient} to the specified {@link Field}.
   * 
   * @param context The execution context.
   * @param testInstance The target test instance.
   * @param field The field to inject.
   */
  default void injectKubernetesClient(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(KubernetesClient.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject"))
        .findAny().isPresent()) {
      return;
    }

    field.setAccessible(true);
    try {
      field.set(testInstance, getKubernetesClient(context));
    } catch (IllegalAccessException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Gets or creates an instance of {@link KubernetesClient} from the {@link ExtensionContext}.
   * 
   * @param context The context.
   * @return An instance of the client.
   */
  default KubernetesClient getKubernetesClient(ExtensionContext context) {
    Object client = context.getStore(DEKORATE_STORE).get(KUBERNETES_CLIENT);
    if (client instanceof KubernetesClient) {
      return (KubernetesClient) client;
    }

    client = new KubernetesClientBuilder().build();
    context.getStore(DEKORATE_STORE).put(KUBERNETES_CLIENT, client);
    return (KubernetesClient) client;
  }

  default void closeKubernetesClient(ExtensionContext context) {
    Object client = context.getStore(DEKORATE_STORE).remove(KUBERNETES_CLIENT);
    if (client instanceof KubernetesClient) {
      ((KubernetesClient) client).close();
    }
  }

  /**
   * Wait until the specified resources satisfy the specified predicate.
   * Workaround for https://github.com/fabric8io/kubernetes-client/issues/1607.
   * 
   * @param context The context.
   * @param items The items.
   * @param condition The condition.
   * @param amount The amount of time to wait.
   * @param timeUnit The time unit of the amount of time to wait.
   * @return true if condition was met.
   */
  default <T extends HasMetadata> boolean waitUntilCondition(ExtensionContext context, Collection<T> items,
      Predicate<T> condition, long amount, TimeUnit timeUnit) throws InterruptedException {
    long amountInNanos = timeUnit.toNanos(amount);
    long end = System.nanoTime() + amountInNanos;

    KubernetesClient client = getKubernetesClient(context);
    List<T> notReady = items.stream().map(i -> client.resource(i).fromServer().get()).filter(condition.negate())
        .collect(Collectors.toList());
    while (System.nanoTime() < end && !notReady.isEmpty()) {
      Thread.sleep(1000);
      notReady = items.stream().map(i -> client.resource(i).fromServer().get()).filter(condition.negate())
          .collect(Collectors.toList());
    }
    return notReady.isEmpty();
  }

  default <T extends HasMetadata> boolean deleteAndWait(ExtensionContext context, T item, long amount, TimeUnit timeUnit)
      throws InterruptedException {
    return deleteAndWait(context, Arrays.asList(item), amount, timeUnit);
  }

  default <T extends HasMetadata> boolean deleteAndWait(ExtensionContext context, Collection<T> items, long amount,
      TimeUnit timeUnit) throws InterruptedException {
    long amountInNanos = timeUnit.toNanos(amount);
    long end = System.nanoTime() + amountInNanos;

    KubernetesClient client = getKubernetesClient(context);

    for (T item : items) {
      try {
        T existing = client.resource(item).fromServer().get();
        if (existing != null) {
          client.resource(existing).delete();
        }
      } catch (KubernetesClientException e) {
        if (e.getCode() != 404) {
          throw e;
        }
      }
    }

    List<T> notDeleted = new ArrayList<>(items);
    while (System.nanoTime() < end && !notDeleted.isEmpty()) {
      for (T item : items) {
        if (!notDeleted.contains(item)) {
          continue;
        }
        try {
          if (client.resource(item).fromServer().get() == null) {
            notDeleted.remove(item);
          }
        } catch (KubernetesClientException e) {
          if (e.getCode() == 404) {
            notDeleted.remove(item);
          }
        }
        Thread.sleep(1000);
      }
    }
    return notDeleted.isEmpty();
  }

}
