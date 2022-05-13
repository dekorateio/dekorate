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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Session;
import io.dekorate.config.AdditionalResourcesLocator;
import io.dekorate.doc.Description;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.spring.config.SpringBootApplicationGenerator;

@Description("Detects Spring Boot and set the runtime attribute to Spring Boot.")
@SupportedAnnotationTypes({ "org.springframework.boot.autoconfigure.SpringBootApplication" })
public class SpringBootApplicationProcessor extends AbstractAnnotationProcessor implements SpringBootApplicationGenerator {

  private static final String SPRING_PROFILE = "spring.profiles.active";

  private final Logger LOGGER = LoggerFactory.getLogger();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        LOGGER.info("Found @SpringBootApplication on: " + mainClass.toString());
      }
    }

    List<String> resourceNames = new ArrayList<>();
    // default resource names:
    resourceNames.add("application.properties");
    resourceNames.add("application.yaml");
    resourceNames.add("application.yml");
    // resource names from active Dekorate features
    resourceNames.addAll(AdditionalResourcesLocator.getAdditionalResources());
    // resource name for Spring profile
    Optional.ofNullable(System.getProperty(SPRING_PROFILE))
        .filter(str -> !str.isEmpty())
        .ifPresent(profile -> {
          resourceNames.add("application-" + profile + ".properties");
          resourceNames.add("application-" + profile + ".yaml");
          resourceNames.add("application-" + profile + ".yml");
        });

    getSession().addPropertyConfiguration(readProperties(resourceNames));
    addPropertyConfiguration(SPRING_BOOT_APPLICATION);
    return false;
  }

  @Override
  public ConfigurationRegistry getConfigurationRegistry() {
    return Session.getSession().getConfigurationRegistry();
  }
}
