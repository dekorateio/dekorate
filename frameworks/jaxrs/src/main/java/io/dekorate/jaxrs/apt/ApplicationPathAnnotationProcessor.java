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

package io.dekorate.jaxrs.apt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.ApplicationPath;

import io.dekorate.Session;
import io.dekorate.doc.Description;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.utils.Strings;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@Description("Detects JAX-RS and servlet annotations and registers the http port.")
@SupportedAnnotationTypes("javax.ws.rs.ApplicationPath")
public class ApplicationPathAnnotationProcessor extends AbstractAnnotationProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = getSession();
    if (roundEnv.processingOver()) {
      session.close();
      return true;
    }

    Set<String> paths = new HashSet<>();
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        ApplicationPath path = mainClass.getAnnotation(ApplicationPath.class);
        if (path != null && Strings.isNotNullOrEmpty(path.value())) {
          Map<String, Object> jaxrs = new HashMap<>();
          Map<String, Object> properties = new HashMap<>();
          properties.put("path", paths.iterator().next());
          getSession().addAnnotationConfiguration(jaxrs);
        }
      }
    }
    return false;
  }
}
