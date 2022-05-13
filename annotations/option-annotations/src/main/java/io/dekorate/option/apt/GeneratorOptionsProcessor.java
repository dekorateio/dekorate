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
package io.dekorate.option.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.WithSession;
import io.dekorate.doc.Description;
import io.dekorate.option.annotation.GeneratorOptions;
import io.dekorate.processor.AbstractAnnotationProcessor;

@Description("Processing generator options, which are used for customizing the generation process")
@SupportedAnnotationTypes({
    "io.dekorate.annotation.Dekorate",
    "io.dekorate.kubernetes.annotation.KubernetesApplication",
    "io.dekorate.openshift.annotation.OpenshiftApplication",
    "io.dekorate.knative.annotation.KnativeApplication",
    "io.dekorate.option.annotation.GeneratorOptions"
})
public class GeneratorOptionsProcessor extends AbstractAnnotationProcessor implements WithSession {

  private static final String INPUT_DIR = "dekorate.input.dir";
  private static final String OUTPUT_DIR = "dekorate.output.dir";

  private static final String FALLBACK_INPUT_DIR = "META-INF/fabric8";
  private static final String FALLBACK_OUTPUT_DIR = "META-INF/fabric8";

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        GeneratorOptions options = mainClass.getAnnotation(GeneratorOptions.class);
        if (options == null) {
          continue;
        }

        process("options", mainClass, GeneratorOptions.class);
        return false;
      }
    }
    return false;
  }
}
