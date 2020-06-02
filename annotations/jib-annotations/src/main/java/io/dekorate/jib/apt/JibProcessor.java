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
package io.dekorate.jib.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.jib.adapter.JibBuildConfigAdapter;
import io.dekorate.jib.annotation.JibBuild;
import io.dekorate.jib.config.JibBuildConfig;
import io.dekorate.jib.generator.JibGenerator;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.project.ApplyProjectInfo;

@SupportedAnnotationTypes("io.dekorate.jib.annotation.JibBuild")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JibProcessor extends AbstractAnnotationProcessor implements JibGenerator {


  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }

    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        JibBuild jib = mainClass.getAnnotation(JibBuild.class);
        if (jib == null) {
          continue;
        }
        add(mainClass);
      }
    }
    return false;
  }

  @Override
  public void add(Element element) {
    JibBuild jib = element.getAnnotation(JibBuild.class);
    if (jib != null) {
      ConfigurationSupplier<JibBuildConfig> config = new AnnotationConfiguration<>(JibBuildConfigAdapter.newBuilder(jib)
                                                                                 .accept(new ApplyProjectInfo(getProject()))
                                                                                 .accept(new ApplyBuildToImageConfiguration()));
      on(config);
    }
  }
}

