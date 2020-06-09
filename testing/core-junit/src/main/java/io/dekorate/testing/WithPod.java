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

import io.dekorate.DekorateException;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.dekorate.testing.annotation.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.util.Optional;

import static java.util.Arrays.stream;

/**
 * Mixin for storing / loading the KubernetesList to context.
 * It also provides methods for injecting the list.
 */
public interface WithPod extends TestInstancePostProcessor, WithBaseConfig, WithKubernetesClient, WithClosables {

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
      .forEach(f -> injectPod(context, testInstance, f));
  }

  /**
   * Inject a {@link Pod} to the specified {@link Field}.
   * The pod is matched using its corresponding endpoints.
   * In other words this acts like `inject pod of service`
   * @param context      The execution context.
   * @param testInstance The target test instance.
   * @param field        The field to inject.
   */
  default void injectPod(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(Pod.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    //Note: we don't require the exact annotation. Any annotation named Inject will do (be it javax, guice etc)
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject")).findAny().isPresent()) {
      return;
    }

    String name = namedAnnotation(field).orElseGet(() -> getName());
    field.setAccessible(true);
    try {
      field.set(testInstance, podForName(context, name));
    } catch (IllegalAccessException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  default Pod podForName(ExtensionContext context, String service) {
    KubernetesClient client = getKubernetesClient(context);
    Endpoints endpoints = client.endpoints().withName(service).get();
    if (endpoints != null) {
      String pod = endpoints.getSubsets().stream()
        .flatMap(s -> s.getAddresses().stream())
        .filter(a -> a.getTargetRef().getKind().equals("Pod"))
        .map(a -> a.getTargetRef().getName())
        .findAny().orElseThrow(() -> new IllegalStateException("Failed to detect pod for service:" + service));

      return client.pods().withName(pod).get();
    }
    throw new IllegalStateException("Failed to detect endpoints for service:" + service);
  }


  /**
   * Returns the value of the {@link Named} annotation.
   * @param field The target field.
   * @return      An optional string with the name if the field is annotated or empty otherwise.
   */
  default Optional<String> namedAnnotation(Field field) {
    return stream(field.getDeclaredAnnotations())
      .filter(a -> a.annotationType().isAssignableFrom(Named.class))
      .map(a -> field.getAnnotation(Named.class).value())
      .findFirst();
  }

  /**
   * @return the resource name.
   */
  String getName();

}

