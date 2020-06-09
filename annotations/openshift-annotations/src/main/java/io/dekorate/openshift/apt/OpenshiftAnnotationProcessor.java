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
package io.dekorate.openshift.apt;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.doc.Description;
import io.dekorate.openshift.annotation.OpenshiftApplication;
import io.dekorate.processor.AbstractAnnotationProcessor;

@Description("Generates openshift manifests.")
@SupportedAnnotationTypes("io.dekorate.openshift.annotation.OpenshiftApplication")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class OpenshiftAnnotationProcessor extends AbstractAnnotationProcessor {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if  (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }
    Set<Element> mainClasses = new HashSet<>();
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        mainClasses.add(mainClass);
      }
    }

    for (Element mainClass : mainClasses) {
      process("openshift", mainClass, OpenshiftApplication.class);
    }
    return false;

  }
}
