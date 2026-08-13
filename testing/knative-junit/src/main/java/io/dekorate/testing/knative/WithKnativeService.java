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

import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.dekorate.DekorateException;
import io.dekorate.testing.WithBaseConfig;
import io.dekorate.testing.WithClosables;
import io.dekorate.testing.WithDiagnostics;
import io.dekorate.testing.annotation.Named;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.knative.serving.v1.Service;

/**
 * Mixin for storing / loading the Knative Service to context.
 * It also provides methods for injecting the list.
 */
public interface WithKnativeService
    extends TestInstancePostProcessor, WithBaseConfig, WithKnativeClient, WithClosables, WithDiagnostics {

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
        .forEach(f -> injectKnativeService(context, testInstance, f));
  }

  /**
   * Inject a {@link Service} to the specified {@link Field}.
   * The pod is matched using its corresponding endpoints.
   * In other words this acts like `inject pod of service`
   * 
   * @param context The execution context.
   * @param testInstance The target test instance.
   * @param field The field to inject.
   */
  default void injectKnativeService(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(Service.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    //Note: we don't require the exact annotation. Any annotation named Inject will do (be it javax, guice etc)
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject"))
        .findAny().isPresent()) {
      return;
    }

    String name = namedAnnotationForKnativeService(field).orElseGet(() -> getName(context));
    field.setAccessible(true);
    try {
      field.set(testInstance, knativeServiceForName(context, name));
    } catch (IllegalAccessException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  default Service knativeServiceForName(ExtensionContext context, String service) {
    KnativeClient client = getKnativeClient(context);
    return client.services().withName(service).get();
  }

  /**
   * Returns the value of the {@link Named} annotation.
   *
   * @param field The target field.
   * @return An optional string with the name if the field is annotated or empty otherwise.
   */
  default Optional<String> namedAnnotationForKnativeService(Field field) {
    return stream(field.getDeclaredAnnotations())
        .filter(a -> a.annotationType().isAssignableFrom(Named.class))
        .map(a -> field.getAnnotation(Named.class).value())
        .findFirst();
  }

  /**
   * @param context The execution context.
   * @return the resource name.
   */
  String getName(ExtensionContext context);

}
