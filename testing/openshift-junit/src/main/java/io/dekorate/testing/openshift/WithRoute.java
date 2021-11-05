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
package io.dekorate.testing.openshift;

import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.dekorate.DekorateException;
import io.dekorate.testing.WithBaseConfig;
import io.dekorate.testing.WithClosables;
import io.dekorate.testing.WithDiagnostics;
import io.dekorate.testing.WithKubernetesClient;
import io.dekorate.testing.annotation.Named;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * Resolves the injection of Routes into test instances.
 */
public interface WithRoute
    extends TestInstancePostProcessor, WithBaseConfig, WithKubernetesClient, WithClosables, WithDiagnostics {

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields()).forEach(f -> injectRoute(context, testInstance, f));
  }

  /**
   * Inject a {@link io.fabric8.openshift.api.model.Route} or a {@link java.net.URL} to the specified {@link Field}.
   * The pod is matched using its corresponding endpoints.
   * In other words this acts like `inject route of service`
   * 
   * @param context The execution context.
   * @param testInstance The target test instance.
   * @param field The field to inject.
   */
  default void injectRoute(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(Route.class) && !field.getType().isAssignableFrom(URL.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    //Note: we don't require the exact annotation. Any annotation named Inject will do (be it javax, guice etc)
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject"))
        .findAny().isPresent()) {
      return;
    }

    String name = namedAnnotationForRoute(field).orElseGet(this::getName);
    Route route = routeForName(context, name);
    field.setAccessible(true);
    try {
      if (field.getType().isAssignableFrom(Route.class)) {
        field.set(testInstance, route);
      } else if (field.getType().isAssignableFrom(URL.class)) {
        String protocol = route.getSpec().getTls() == null ? "http" : "https";
        int port = "http".equals(protocol) ? 80 : 443;
        URL url = new URL(protocol, route.getSpec().getHost(), port, route.getSpec().getPath());
        field.set(testInstance, url);
      }

    } catch (Exception e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  default Route routeForName(ExtensionContext context, String name) {
    OpenShiftClient client = getKubernetesClient(context).adapt(OpenShiftClient.class);
    return client.routes().withName(name).get();
  }

  /**
   * Returns the value of the {@link Named} annotation.
   * 
   * @param field The target field.
   * @return An optional string with the name if the field is annotated or empty otherwise.
   */
  default Optional<String> namedAnnotationForRoute(Field field) {
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
