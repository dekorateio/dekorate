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

package io.ap4k.istio.apt;

import io.ap4k.istio.generator.IstioConfigGenerator;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.annotation.processing.RoundEnvironment;
import java.util.Set;
import javax.lang.model.element.Element;

@Description("Inject istio proxy to pods.")
@SupportedAnnotationTypes("io.ap4k.istio.annotation.Istio")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class IstioAnnotationProcessor extends  AbstractAnnotationProcessor implements IstioConfigGenerator {

  @Override
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
