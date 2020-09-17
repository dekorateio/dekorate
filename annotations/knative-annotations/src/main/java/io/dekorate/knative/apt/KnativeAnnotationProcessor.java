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
package io.dekorate.knative.apt;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.doc.Description;
import io.dekorate.knative.adapter.KnativeConfigAdapter;
import io.dekorate.knative.annotation.KnativeApplication;
import io.dekorate.knative.generator.KnativeApplicationGenerator;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyImagePullSecretConfiguration;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.utils.Maps;

@Description("Generates knative manifests.")
@SupportedAnnotationTypes("io.dekorate.knative.annotation.KnativeApplication")
public class KnativeAnnotationProcessor extends AbstractAnnotationProcessor {

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
      process("knative", mainClass, KnativeApplication.class);
    }
    return false;
  }

}

