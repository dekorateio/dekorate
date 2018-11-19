/**
 * Copyright (C) 2018 Ioannis Canellos 
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package io.ap4k.openshift.processor;

import io.ap4k.Session;
import io.ap4k.annotation.KubernetesApplication;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.openshift.OpenshiftGenerator;
import io.ap4k.openshift.SourceToImageGenerator;
import io.ap4k.openshift.adapt.SourceToImageConfigAdapter;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.openshift.annotation.SourceToImage;
import io.ap4k.openshift.confg.OpenshiftConfigCustomAdapter;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.SourceToImageConfig;
import io.ap4k.openshift.config.SourceToImageConfigBuilder;
import io.ap4k.openshift.visitor.ApplyOpenshiftConfig;
import io.ap4k.processor.AbstractAnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static io.ap4k.openshift.adapt.SourceToImageConfigAdapter.newBuilder;

@SupportedAnnotationTypes("io.ap4k.openshift.annotation.SourceToImage")
public class SourceToImageAnnotationProcessor extends AbstractAnnotationProcessor<SourceToImageConfig> {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Session session = Session.getSession();
        if  (roundEnv.processingOver()) {
            session.onClose(r -> write(r));
            return true;
        }
        for (TypeElement typeElement : annotations) {
            for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
              session.configurations().add(configuration(mainClass));
              session.generators().add(new SourceToImageGenerator(session.resources()));
            }
        }
        return false;
    }

  @Override
  public ConfigurationSupplier<SourceToImageConfig> configuration(Element mainClass) {
    return new ConfigurationSupplier<SourceToImageConfig>(configurationBuilder(mainClass));
  }

  /**
     * Get or newBuilder a new config for the specified {@link Element}.
     * @param mainClass     The type element of the annotated class (Main).
     * @return              A new config.
     */
    public SourceToImageConfigBuilder configurationBuilder(Element mainClass) {
        SourceToImage sourceToImage = mainClass.getAnnotation(SourceToImage.class);
      OpenshiftApplication openshiftApplication = mainClass.getAnnotation(OpenshiftApplication.class);
      KubernetesApplication kubernetesApplication = mainClass.getAnnotation(KubernetesApplication.class);
      OpenshiftConfig openshiftConfig = OpenshiftConfigCustomAdapter.newBuilder(project, openshiftApplication, kubernetesApplication).build();
        return SourceToImageConfigAdapter.newBuilder(sourceToImage).accept(new ApplyOpenshiftConfig(openshiftConfig));
    }
}
