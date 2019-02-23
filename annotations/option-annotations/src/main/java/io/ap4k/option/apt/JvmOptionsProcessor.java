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

package io.ap4k.option.apt;

import io.ap4k.WithSession;
import io.ap4k.doc.Description;
import io.ap4k.option.annotation.JvmOptions;
import io.ap4k.option.generator.JvmOptionsGenerator;
import io.ap4k.processor.AbstractAnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Description("Jvm options, which are used for the target deployment.")
@SupportedAnnotationTypes("io.ap4k.option.annotation.JvmOptions")
public class JvmOptionsProcessor extends AbstractAnnotationProcessor implements JvmOptionsGenerator, WithSession {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      session.close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        JvmOptions options = mainClass.getAnnotation(JvmOptions.class);
        if (options != null) {
          add(mainClass);
        }
      }
    }
    return false;
  }
}
