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

package io.ap4k.spring.apt;

import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.Session;
import io.ap4k.doc.Description;
import io.ap4k.spring.generator.SpringBootWebAnnotationGenerator;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Description("Detects Spring Boot web endpoints and registers the http port.")
@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.RequestMapping", "org.springframework.web.bind.annotation.GetMapping"})
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
