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
package io.dekorate.testing.knative;

import static io.dekorate.testing.Testing.DEKORATE_STORE;
import static java.util.Arrays.stream;

import java.lang.reflect.Field;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.dekorate.DekorateException;
import io.dekorate.testing.WithKubernetesClient;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.BaseClient;

/**
 * Mixin for storing the knative client into the context.
 * It also provides functionality for injecting the client.
 */
public interface WithKnativeClient extends TestInstancePostProcessor, WithKubernetesClient {

  String KNATIVE_CLIENT = "KNATIVE_CLIENT";

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
        .forEach(f -> injectKnativeClient(context, testInstance, f));
  }

  /**
   * Inject an instance of {@link KnativeClient} to the specified {@link Field}.
   * 
   * @param context The execution context.
   * @param testInstance The target test instance.
   * @param field The field to inject.
   */
  default void injectKnativeClient(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(KnativeClient.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject"))
        .findAny().isPresent()) {
      return;
    }

    field.setAccessible(true);
    try {
      field.set(testInstance, getKnativeClient(context));
    } catch (IllegalAccessException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Gets or creates an instance of {@link KnativeClient} from the {@link ExtensionContext}.
   * 
   * @param context The context.
   * @return An instance of the client.
   */
  default KnativeClient getKnativeClient(ExtensionContext context) {
    Object client = context.getStore(DEKORATE_STORE).get(KNATIVE_CLIENT);
    if (client instanceof KnativeClient) {
      return (KnativeClient) client;
    }

    KnativeClient knativeClient = getKubernetesClient(context).adapt(KnativeClient.class);
    context.getStore(DEKORATE_STORE).put(KNATIVE_CLIENT, knativeClient);
    return knativeClient;
  }

  default void closeKnativeClient(ExtensionContext context) {
    Object client = context.getStore(DEKORATE_STORE).remove(KNATIVE_CLIENT);
    if (client instanceof KnativeClient) {
      ((BaseClient) client).close();
    }
  }

}
