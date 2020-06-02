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
package io.dekorate.prometheus.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.prometheus.adapter.ServiceMonitorConfigAdapter;
import io.dekorate.prometheus.annotation.EnableServiceMonitor;
import io.dekorate.prometheus.config.ServiceMonitorConfigBuilder;
import io.dekorate.prometheus.generator.ServiceMonitorGenerator;

@SupportedAnnotationTypes({"io.dekorate.prometheus.annotation.EnableServiceMonitor"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ServiceMonitorAnnotationProcessor extends AbstractAnnotationProcessor implements ServiceMonitorGenerator {

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

  public void add(Element element) {
    EnableServiceMonitor serviceMonitor = element.getAnnotation(EnableServiceMonitor.class);
    on(serviceMonitor != null
      ? new AnnotationConfiguration<>(ServiceMonitorConfigAdapter.newBuilder(serviceMonitor))
      : new AnnotationConfiguration<>(new ServiceMonitorConfigBuilder()));
  }
}
