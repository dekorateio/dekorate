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
package io.dekorate.kind.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.doc.Description;
import io.dekorate.kind.annotation.KindApplication;
import io.dekorate.processor.AbstractAnnotationProcessor;

@Description("Generates kubernetes manifests.")
@SupportedAnnotationTypes("io.dekorate.kind.annotation.KindApplication")
public class KindAnnotationProcessor extends AbstractAnnotationProcessor {

  private final Logger LOGGER = LoggerFactory.getLogger();

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        LOGGER.info("Found @KindApplication on: " + mainClass.toString());
        process("kind", mainClass, KindApplication.class);
      }
    }
    return false;
  }
}
