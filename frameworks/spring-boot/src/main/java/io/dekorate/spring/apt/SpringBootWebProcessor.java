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

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import io.dekorate.Session;
import io.dekorate.doc.Description;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.spring.generator.SpringBootWebAnnotationGenerator;

@Description("Detects Spring Boot web endpoints and registers the http port.")
@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.RequestMapping", "org.springframework.web.bind.annotation.GetMapping", "org.springframework.data.rest.core.annotation.RepositoryRestResource", "javax.ws.rs.GET", "javax.ws.rs.POST", "javax.ws.rs.PUT", "javax.ws.rs.DELETE", "javax.ws.rs.OPTIONS", "javax.ws.rs.HEAD", "javax.ws.rs.PATCH"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SpringBootWebProcessor extends AbstractAnnotationProcessor implements SpringBootWebAnnotationGenerator {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.close();
      return true;
    }
    add(WEB_ANNOTATIONS);
    return false;
  }

}
