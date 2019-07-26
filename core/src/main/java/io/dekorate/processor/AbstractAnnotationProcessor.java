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
package io.dekorate.processor;

import io.dekorate.DekorateException;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.KubernetesResource;
import io.dekorate.project.AptProjectFactory;
import io.dekorate.utils.Maps;
import io.dekorate.utils.Serialization;
import io.dekorate.deps.kubernetes.api.model.KubernetesList;
import io.dekorate.utils.Urls;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor implements WithProject  {

  protected static final String PACKAGE = "";
  protected static final String PROJECT = "META-INF/dekorate/.project.%s";
  protected static final String JSON = "json";
  protected static final String YML = "yml";
  protected static final String TMP = "tmp";
  protected static final String DOT = ".";

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    if (!projectExists()) {
      setProject(AptProjectFactory.create(processingEnv));
    }

    Session session = Session.getSession();
    if (!session.hasWriter()) {
      session.setWriter(new AptWriter(processingEnv));
    }
  }

  protected List<HasMetadata> read(String path) {
    try (InputStream is = new FileInputStream(getProject().getBuildInfo().getResourceDir().resolve(path).toFile())) {
        KubernetesResource resource = Serialization.unmarshal(is, KubernetesResource.class);
        if (resource instanceof KubernetesList) {
          return ((KubernetesList) resource).getItems();
        } else if (resource instanceof HasMetadata) {
          return Arrays.asList((HasMetadata)resource);
        } else {
          return Collections.<HasMetadata>emptyList();
        }
    } catch (IOException e) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, path + " JSON not found.");
    }
    return null;
  }

  /**
   * @return the application properties
   */
  protected Map<String, Object> readApplicationConfig(String... resourceNames) {
    Map<String, Object> result = new HashMap<>();
    for (String resourceName : resourceNames) {
      try (InputStream is = new FileInputStream(getProject().getBuildInfo().getResourceDir().resolve(resourceName).toFile())) {
        if (resourceName.endsWith(".properties")) {
          Map<String, Object> newProps = Maps.fromProperties(is);
          mergeProperties(result, newProps);
        } else if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
          Map<String, Object> newProps = Maps.fromYaml(is);
          mergeProperties(result, newProps);
        } else {
          throw new IllegalArgumentException("Illegal resource name:" + resourceName + ". It needs to be properties or yaml file.");
        }
      } catch (FileNotFoundException | NoSuchFileException e) {
        continue;
      } catch (Exception e) {
        throw DekorateException.launderThrowable(e);
      } 
    }
    return result;
  }

  private void mergeProperties(Map<String, Object> result, Map<String, Object> newProps) {
    for(String newKey : newProps.keySet()) {
      if(result.containsKey(newKey) && Map.class.isInstance(result.get(newKey)) && Map.class.isInstance(newProps.get(newKey))) {
        mergeProperties((Map)result.get(newKey), (Map)newProps.get(newKey));
      } else {
        result.putAll(newProps);
      }
    }
  }

  /**
   * Get the output directory of the processor.
   * @return  The directroy.
   */
  public Path getOutputDirectory() {
    try {
      FileObject project = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, PACKAGE, String.format(PROJECT, TMP));
      return Paths.get(Urls.toFile(project.toUri().toURL()).getParentFile().getAbsolutePath());
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }
}
