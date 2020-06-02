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
package io.dekorate.kubernetes.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.adapter.KubernetesConfigAdapter;
import io.dekorate.kubernetes.annotation.KubernetesApplication;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.kubernetes.configurator.ApplyDeployToApplicationConfiguration;
import io.dekorate.kubernetes.configurator.ApplyImagePullSecretConfiguration;
import io.dekorate.kubernetes.generator.KubernetesApplicationGenerator;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.project.ApplyProjectInfo;

@Description("Generates kubernetes manifests.")
@SupportedAnnotationTypes("io.dekorate.kubernetes.annotation.KubernetesApplication")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class KubernetesAnnotationProcessor extends AbstractAnnotationProcessor implements KubernetesApplicationGenerator {

  private final Logger LOGGER = LoggerFactory.getLogger();

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        LOGGER.info("Found @KubernetesApplication on: " + mainClass.toString());
        add(mainClass);
      }
    }
    return false;
  }

  public void add(Element element) {
    KubernetesApplication application = element.getAnnotation(KubernetesApplication.class);
     add(new AnnotationConfiguration<>(
            KubernetesConfigAdapter
            .newBuilder(application)
            .accept(new ApplyBuildToImageConfiguration())
            .accept(new ApplyImagePullSecretConfiguration())
            .accept(new ApplyDeployToApplicationConfiguration())
            .accept(new ApplyProjectInfo(getProject()))));
  }
}
