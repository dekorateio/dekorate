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
package io.dekorate.servicecatalog.apt;


import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.servicecatalog.adapter.ServiceCatalogConfigAdapter;
import io.dekorate.servicecatalog.annotation.ServiceCatalog;
import io.dekorate.servicecatalog.config.ServiceCatalogConfigBuilder;
import io.dekorate.servicecatalog.generator.ServiceCatalogGenerator;

@SupportedAnnotationTypes({"io.dekorate.servicecatalog.annotation.ServiceCatalog", "io.dekorate.servicecatalog.annotation.ServiceCatalogInstance"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ServiceCatalogAnnotationProcessor extends AbstractAnnotationProcessor implements ServiceCatalogGenerator {

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      getSession().close();
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        add(mainClass);
      }
    }
    return false;
  }

  @Override
  public void add(Element element) {
    ServiceCatalog serviceCatalog = element.getAnnotation(ServiceCatalog.class);
    on(serviceCatalog != null
      ? new AnnotationConfiguration<>(ServiceCatalogConfigAdapter.newBuilder(serviceCatalog))
      : new AnnotationConfiguration<>(new ServiceCatalogConfigBuilder()));
  }
}
