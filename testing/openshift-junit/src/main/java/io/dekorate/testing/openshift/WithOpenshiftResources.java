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
package io.dekorate.testing.openshift;

import static io.dekorate.testing.Testing.DEKORATE_STORE;
import static io.dekorate.testing.Testing.KUBERNETES_LIST;
import static java.util.Arrays.stream;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.dekorate.DekorateException;
import io.dekorate.project.Project;
import io.dekorate.testing.WithProject;
import io.dekorate.utils.Serialization;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;

/**
 * Mixin for storing / loading the KubernetesList to context.
 * It also provides methods for injecting the list.
 */
public interface WithOpenshiftResources extends TestInstancePostProcessor, WithProject {

  String MANIFEST_PATH = "openshift.yml";

  default void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
    stream(testInstance.getClass().getDeclaredFields())
        .forEach(f -> injectOpenshiftResources(context, testInstance, f));
  }

  /**
   * Inject an instance of {@link KubernetesList} to the specified {@link Field}.
   *
   * @param context The execution context.
   * @param testInstance The target test instance.
   * @param field The field to inject.
   */
  default void injectOpenshiftResources(ExtensionContext context, Object testInstance, Field field) {
    if (!field.getType().isAssignableFrom(KubernetesList.class)) {
      return;
    }

    //This is to make sure we don't write on fields by accident.
    //Note: we don't require the exact annotation. Any annotation named Inject will do (be it javax, guice etc)
    if (!stream(field.getDeclaredAnnotations()).filter(a -> a.annotationType().getSimpleName().equalsIgnoreCase("Inject"))
        .findAny().isPresent()) {
      return;
    }

    List<Project> projects = getProjects(context);
    List<HasMetadata> items = new ArrayList<>();
    for (Project project : projects) {
      items.addAll(getOpenshiftResources(context, project).getItems());
    }

    field.setAccessible(true);
    try {
      KubernetesList list = new KubernetesList();
      list.setItems(items);
      field.set(testInstance, list);
    } catch (IllegalAccessException e) {
      throw DekorateException.launderThrowable(e);
    }
  }

  /**
   * Gets or creates an instance of {@link KubernetesList} with all generated resources.
   *
   * @param context The context.
   * @param project project to load the manifest from.
   * @return An instance of the list.
   */
  default KubernetesList getOpenshiftResources(ExtensionContext context, Project project) {
    String key = KUBERNETES_LIST + project.getRoot();
    Object list = context.getStore(DEKORATE_STORE).get(key);
    if (list instanceof KubernetesList) {
      return (KubernetesList) list;
    }

    list = fromManifest(project);
    context.getStore(DEKORATE_STORE).put(key, list);
    return (KubernetesList) list;
  }

  /**
   * Load an unmarshal the {@KubernetesList} from the manifest file.
   *
   * @param project project to load the manifest from.
   * @return The kubernetes list if found or an empty kubernetes list otherwise.
   */
  default KubernetesList fromManifest(Project project) {
    KubernetesList result = new KubernetesList();

    Path manifestUrl = project.getRoot().resolve("target/classes").resolve(project.getDekorateOutputDir())
        .resolve(MANIFEST_PATH);
    if (!Files.exists(manifestUrl)) {
      return result;
    }

    System.out.println("Apply test resources from:" + manifestUrl);
    try (InputStream is = manifestUrl.toUri().toURL().openStream()) {
      result = Serialization.unmarshalAsList(is);
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
    return result;
  }
}
