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

package io.ap4k.istio.processor;

import io.ap4k.istio.handler.IstioHandler;
import io.ap4k.kubernetes.config.ConfigurationSupplier;
import io.ap4k.istio.adapter.IstioConfigAdapter;
import io.ap4k.istio.annotation.Istio;
import io.ap4k.istio.config.IstioConfig;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.annotation.processing.RoundEnvironment;
import java.util.Set;
import javax.lang.model.element.Element;
import io.ap4k.Session;

@Description("Inject istio proxy to pods.")
@SupportedAnnotationTypes("io.ap4k.istio.annotation.Istio")
public class IstioAnnotationProcessor extends  AbstractAnnotationProcessor<IstioConfig> {



  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if  (roundEnv.processingOver()) {
      Session session = Session.getSession();
      session.onClose(this::write);
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        Session session = Session.getSession();
        session.configurators().add(config(mainClass));
        session.handlers().add(new IstioHandler(session.resources()));
      }
    }
    return false;
  }

  public ConfigurationSupplier<IstioConfig> config(Element mainClass) {
    Istio istio = mainClass.getAnnotation(Istio.class);
    return istio != null
      ? new ConfigurationSupplier<>(IstioConfigAdapter.newBuilder(istio))
      : new ConfigurationSupplier<>(IstioConfig.newIstioConfigBuilder());
  }
}
