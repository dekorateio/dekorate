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

package io.dekorate.spring.apt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import io.dekorate.doc.Description;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.spring.BeanListener;

@Description("Detects Spring Boot and set the runtime attribute to Spring Boot.")
@SupportedAnnotationTypes({"org.springframework.context.annotation.Bean"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SpringBootBeanProcessor extends AbstractAnnotationProcessor {

  private Map<String, BeanListener> listeners = new HashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if  (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }

    registerListeners();
    for (TypeElement typeElement : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
        if (element instanceof ExecutableElement) {
          ExecutableElement executableElement = (ExecutableElement) element;
          TypeMirror typeMirror = executableElement.getReturnType();
          Element el = processingEnv.getTypeUtils().asElement(typeMirror);
          if (el instanceof TypeElement) {
            TypeElement te = (TypeElement) el;
            String fqcn = te.getQualifiedName().toString();
            BeanListener listener = listeners.get(fqcn);
            if (listener != null) {
              listener.onBean();
            }
          }
        }
      }
    }
    return false;
  }

  private void registerListeners() {
    if (listeners.isEmpty()) {
      ServiceLoader<BeanListener> serviceLoader = ServiceLoader.load(BeanListener.class, BeanListener.class.getClassLoader());
      Iterator<BeanListener> iterator = serviceLoader.iterator();
      while (iterator.hasNext()) {
        BeanListener listener = iterator.next();
        listeners.put(listener.getType(), listener);
      }
    }
  }
}
