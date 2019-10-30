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
package io.dekorate.testing.kubernetes;

import io.dekorate.DekorateException;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.testing.WithProject;
import io.dekorate.utils.Serialization;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;

import static io.dekorate.testing.Testing.Dekorate_STORE;
import static java.util.Arrays.stream;

/**
 * Mixin for storing / loading the KubernetesList to context.
 * It also provides methods for injecting the list.
 */
public interface WithKubernetesResources extends TestInstancePostProcessor, WithProject {

  String MANIFEST_PATH = "kubernetes.yml";
  String KUBERNETES_LIST = "KUBERNETES_LIST";

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
      .forEach(f -> injectKubernetesResources(context, testInstance, f));
  }

  /**
   * Inject an instance of {@link KubernetesList} to the specified {@link Field}.
   *
   * @param context      The execution context.
   * @param testInstance The target test instance.
   * @param field        The field to inject.
   */
  default void injectKubernetesResources(ExtensionContext context, Object testInstance, Field field) {
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
      field.set(testInstance, getKubernetesResources(context));
    } catch (IllegalAccessException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Gets or creates an instance of {@link KubernetesList} with all generated resources.
   *
   * @param context The context.
   * @return An instance of the list.
   */
  default KubernetesList getKubernetesResources(ExtensionContext context) {
    Object list = context.getStore(Dekorate_STORE).get(KUBERNETES_LIST);
    if (list instanceof KubernetesList) {
      return (KubernetesList) list;
    }

    list = fromManifest();
    context.getStore(Dekorate_STORE).put(KUBERNETES_LIST, list);
    return (KubernetesList) list;
  }

  /**
   * Load an unmarshal the {@KubernetesList} from the manifest file.
   * @return  The kubernetes list if found or an empty kubernetes list otherwise.
   */
  default KubernetesList fromManifest() {
    KubernetesList result = new KubernetesList();
    URL manifestUrl = WithKubernetesResources.class.getClassLoader().getResource(getProject().getDekorateOutputDir() + File.separatorChar + MANIFEST_PATH);
    if (manifestUrl == null)  {
      return result;
    }

    System.out.println("Apply test resources from:" + manifestUrl);
    try (InputStream is = manifestUrl.openStream()) {
      result = Serialization.unmarshalAsList(is);
    } catch (IOException e) {
       throw DekorateException.launderThrowable(e);
    }
    return result;
  }
}

