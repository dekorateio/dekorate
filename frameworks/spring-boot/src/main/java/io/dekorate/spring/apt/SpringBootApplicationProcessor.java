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

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.doc.Description;
import io.dekorate.spring.generator.SpringBootApplicationGenerator;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import java.util.Set;

@Description("Detects Spring Boot and set the runtime attribute to Spring Boot.")
@SupportedAnnotationTypes({"org.springframework.boot.autoconfigure.SpringBootApplication"})
public class SpringBootApplicationProcessor extends AbstractAnnotationProcessor implements SpringBootApplicationGenerator {

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
    getSession().addPropertyConfiguration(readApplicationConfig("application.properties",
                                         "application.yaml",
                                         "application.yml", 
                                         "application-kubernetes.properties",
                                         "application-kubernetes.yaml",
                                         "application-kubernetes.yml"));
    addPropertyConfiguration(SPRING_BOOT_APPLICATION);
    return false;
  }
}
