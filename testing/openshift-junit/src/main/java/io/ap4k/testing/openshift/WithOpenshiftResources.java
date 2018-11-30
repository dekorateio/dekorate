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
package io.ap4k.testing.openshift;

import io.ap4k.Ap4kException;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.utils.Serialization;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;

import static io.ap4k.testing.Testing.AP4K_STORE;
import static java.util.Arrays.stream;

/**
 * Mixin for storing / loading the KubernetesList to context.
 * It also provides methods for injecting the list.
 */
public interface WithOpenshiftResources extends TestInstancePostProcessor {

  String MANIFEST_PATH = "META-INF/ap4k/openshift.yml";
  String KUBERNETES_LIST = "KUBERNETES_LIST";

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
      .forEach(f -> injectOpenshiftResources(context, testInstance, f));
  }

  /**
   * Inject an instance of {@link KubernetesList} to the specified {@link Field}.
   *
   * @param context      The execution context.
   * @param testInstance The target test instance.
   * @param field        The field to inject.
   */
  default void injectOpenshiftResources(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(KubernetesList.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    //Note: we don't require the exact annotation. Any annotation named Inject will do (be it javax, guice etc)
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject")).findAny().isPresent()) {
      return;
    }

    field.setAccessible(true);
    try {
      field.set(testInstance, getOpenshiftResources(context));
    } catch (IllegalAccessException e) {
      throw Ap4kException.launderThrowable(e);
    }
  }

  /**
   * Gets or creates an instance of {@link KubernetesList} with all generated resources.
   *
   * @param context The context.
   * @return An instance of the list.
   */
  default KubernetesList getOpenshiftResources(ExtensionContext context) {
    Object list = context.getStore(AP4K_STORE).get(KUBERNETES_LIST);
    if (list instanceof KubernetesList) {
      return (KubernetesList) list;
    }

    list = fromManifest();
    context.getStore(AP4K_STORE).put(KUBERNETES_LIST, list);
    return (KubernetesList) list;
  }

  /**
   * Load an unmarshal the {@KubernetesList} from the manifest file.
   * @return  The kubernetes list if found or an empty kubernetes list otherwise.
   */
  default KubernetesList fromManifest() {
    KubernetesList result = new KubernetesList();
    URL manifestUrl = WithOpenshiftResources.class.getClassLoader().getResource(MANIFEST_PATH);
    if (manifestUrl == null)  {
      return result;
    }

    System.out.println("Apply test resources from:" + manifestUrl);
    try (InputStream is = manifestUrl.openStream()) {
        result = Serialization.unmarshal(is, KubernetesList.class);
    } catch (IOException e) {
       throw Ap4kException.launderThrowable(e);
    }
    return result;
  }
}

