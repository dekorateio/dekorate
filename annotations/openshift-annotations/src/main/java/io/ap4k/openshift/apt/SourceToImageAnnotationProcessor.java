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

package io.ap4k.openshift.apt;

import io.ap4k.openshift.generator.S2iBuildGenerator;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Description("Adds source to image config in the openshift manifests.")
@SupportedAnnotationTypes("io.ap4k.openshift.annotation.EnableS2iBuild")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SourceToImageAnnotationProcessor extends AbstractAnnotationProcessor implements S2iBuildGenerator {
  public static String DEFAULT_S2I_BUILDER_IMAGE = "fabric8/s2i-java:2.3";

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if  (roundEnv.processingOver()) {
      session.close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        add(mainClass);
      }
    }
    return false;
  }
}
