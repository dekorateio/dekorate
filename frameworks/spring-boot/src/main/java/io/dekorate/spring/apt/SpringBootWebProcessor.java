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
package io.dekorate.spring.apt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.doc.Description;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.spring.config.SpringBootWebAnnotationGenerator;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@Description("Detects Spring Boot web endpoints and registers the http port.")
@SupportedAnnotationTypes({ "org.springframework.web.bind.annotation.RestController",
    "org.springframework.web.bind.annotation.RequestMapping", "org.springframework.web.bind.annotation.GetMapping",
    "org.springframework.data.rest.core.annotation.RepositoryRestResource", "javax.ws.rs.GET", "javax.ws.rs.POST",
    "javax.ws.rs.PUT", "javax.ws.rs.DELETE", "javax.ws.rs.OPTIONS", "javax.ws.rs.HEAD", "javax.ws.rs.PATCH" })
public class SpringBootWebProcessor extends AbstractAnnotationProcessor implements SpringBootWebAnnotationGenerator {

  private final Logger LOGGER = LoggerFactory.getLogger();

  private static final String REQUESTMAPPING = "RequestMapping";
  private static final Set<String> PATH_METHODS = new HashSet<String>() {
    {
      add("value");
      add("path");
    }
  };

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }
    Set<String> paths = new HashSet<>();

    for (TypeElement typeElement : annotations) {

      if (typeElement.getSimpleName().toString().endsWith("RestController")) {
        for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
          if (element instanceof TypeElement) {
            Set<String> detectedPaths = element.getAnnotationMirrors()
                .stream()
                .filter(a -> a.getAnnotationType().toString().contains(REQUESTMAPPING))
                .flatMap(a -> a.getElementValues().entrySet().stream())
                .filter(e -> PATH_METHODS.contains(e.getKey().getSimpleName().toString()))
                .map(p -> p.getValue().getValue().toString().replace('"', ' ').trim())
                .collect(Collectors.toSet());

            if (detectedPaths.isEmpty()) {
              paths.add("/");
            } else {
              paths.addAll(detectedPaths);
            }
          }
        }
      }
    }

    LOGGER.info("Found Spring web annotation!");
    Map<String, Object> config = new HashMap<String, Object>();
    config.put(DEKORATE_SPRING_WEB_PATH, findShortedCommonPath(paths));
    addAnnotationConfiguration(config);
    return false;
  }

  /**
   * Find the shortest common path of the specified paths.
   * 
   * @param paths The set of paths
   * @return the shorted common path, or / if there is no common path.
   */
  public static String findShortedCommonPath(Set<String> paths) {
    if (paths.isEmpty()) {
      return "/";
    }
    String longestPath = paths.stream().sorted(Comparator.comparingInt(String::length).reversed()).findFirst()
        .orElseThrow(IllegalStateException::new);
    String shortedPath = longestPath;
    for (String p : paths) {
      if (shortedPath.startsWith(p)) {
        shortedPath = p;
      } else if (!p.startsWith(shortedPath)) {
        return "/";
      }
    }
    return shortedPath;
  }

  @Override
  public ConfigurationRegistry getConfigurationRegistry() {
    return Session.getSession().getConfigurationRegistry();
  }
}
