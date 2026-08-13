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

import static io.dekorate.utils.Maps.fromProperties;
import static io.dekorate.utils.Maps.fromYaml;
import static io.dekorate.utils.Maps.merge;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import io.dekorate.DekorateException;
import io.dekorate.Logger;
import io.dekorate.Session;
import io.dekorate.WithProject;
import io.dekorate.WithSession;
import io.dekorate.project.AptProjectFactory;
import io.dekorate.utils.Maps;
import io.dekorate.utils.Urls;

public abstract class AbstractAnnotationProcessor extends AbstractProcessor implements WithProject, WithSession {

  protected static final String PACKAGE = "";
  protected static final String PROJECT = "META-INF/dekorate/.project.%s";
  protected static final String JSON = "json";
  protected static final String YML = "yml";
  protected static final String TMP = "tmp";
  protected static final String DOT = ".";

  protected Logger LOGGER;

  private final AtomicReference<ProcessingEnvironment> processingEnvRef = new AtomicReference<>();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.processingEnvRef.set(processingEnv);

    if (!projectExists()) {
      setProject(AptProjectFactory.create(processingEnv));
    }
  }

  @Override
  public Session getSession() {
    ProcessingEnvironment processingEnv = processingEnvRef.get();
    if (processingEnv == null) {
      throw new IllegalStateException("No processing environment available.");
    }

    Session session = Session.getSession();
    if (!session.hasReader()) {
      session.setReader(new AptReader(processingEnv));
    }
    if (!session.hasWriter()) {
      session.setWriter(new AptWriter(getProject(), processingEnv));
    }
    return session;
  }

  public <A extends Annotation> void process(String key, Element element, Class<A> annotationClass) {
    getSession().addAnnotationConfiguration(Maps.fromAnnotation(key, element.getAnnotation(annotationClass), annotationClass));
  }

  /**
   * Read the properties from the application configuration (for example: from the application.properties file)
   * and from the dekorate system properties (for example: -Ddekorate.kubernetes.replicas=5)
   *
   * @param resourceNames The resource files where to read the application configuration.
   * @return all the properties from the configuration and the dekorate system properties.
   */
  protected Map<String, Object> readProperties(List<String> resourceNames) {
    // Resolve properties:
    Map<String, Object> props = new HashMap<>();
    // - from application config
    props.putAll(readApplicationConfig(resourceNames.toArray(new String[resourceNames.size()])));
    // - from system properties
    Maps.merge(props, readDekorateSystemProperties());
    return props;
  }

  private Map<String, Object> readApplicationConfig(String... resourceNames) {
    Map<String, Object> result = new HashMap<>();
    for (String resourceName : resourceNames) {
      try (InputStream is = new FileInputStream(
          getProject().getBuildInfo().getResourceDir().resolve(resourceName).toFile())) {
        if (resourceName.endsWith(".properties")) {
          Map<String, Object> newProps = fromProperties(is);
          merge(result, newProps);
        } else if (resourceName.endsWith(".yml") || resourceName.endsWith(".yaml")) {
          Map<String, Object> newProps = Maps.kebabToCamelCase(fromYaml(is));
          merge(result, newProps);
        } else {
          throw new IllegalArgumentException(
              "Illegal resource name:" + resourceName + ". It needs to be properties or yaml file.");
        }
      } catch (FileNotFoundException | NoSuchFileException e) {
        continue;
      } catch (Exception e) {
        throw DekorateException.launderThrowable(e);
      }
    }
    return result;
  }

  private Map<String, Object> readDekorateSystemProperties() {
    return Maps.fromProperties(System.getProperties());
  }

  /**
   * Does exactly the same thing than Maps.merge. Let's just keep Maps.merge which has a wider scope..
   *
   * @deprecated use {@link Maps#merge(Map, Map)} ()} instead.
   */
  @Deprecated
  private void mergeProperties(Map<String, Object> result, Map<String, Object> newProps) {
    for (String newKey : newProps.keySet()) {
      if (result.containsKey(newKey) && Map.class.isInstance(result.get(newKey))
          && Map.class.isInstance(newProps.get(newKey))) {
        mergeProperties((Map) result.get(newKey), (Map) newProps.get(newKey));
      } else {
        result.putAll(newProps);
      }
    }
  }

  /**
   * Get the output directory of the processor.
   * 
   * @return The directroy.
   */
  public Path getOutputDirectory() {
    try {
      FileObject project = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, PACKAGE,
          String.format(PROJECT, TMP));
      return Paths.get(Urls.toFile(project.toUri().toURL()).getParentFile().getAbsolutePath());
    } catch (IOException e) {
      throw DekorateException.launderThrowable(e);
    }
  }
}
