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

package io.ap4k.openshift.processor;

import io.ap4k.Session;
import io.ap4k.kubernetes.config.ConfigurationSupplier;
import io.ap4k.openshift.adapt.S2iConfigAdapter;
import io.ap4k.openshift.annotation.EnableS2iBuild;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.openshift.config.OpenshiftConfigCustomAdapter;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.openshift.config.S2iConfigBuilder;
import io.ap4k.openshift.handler.SourceToImageHandler;
import io.ap4k.openshift.hook.JavaBuildHook;
import io.ap4k.openshift.configurator.ApplySourceToImageHook;
import io.ap4k.openshift.configurator.ApplyOpenshiftConfig;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

@Description("Adds source to image config in the openshift manifests.")
@SupportedAnnotationTypes("io.ap4k.openshift.annotation.EnableS2iBuild")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SourceToImageAnnotationProcessor extends AbstractAnnotationProcessor<S2iConfig> {
  public static String DEFAULT_S2I_BUILDER_IMAGE = "fabric8/s2i-java:2.3";

  public static S2iConfig DEFAULT_SOURCE_TO_IMAGE_CONFIG = new S2iConfigBuilder()
    .withBuilderImage(DEFAULT_S2I_BUILDER_IMAGE)
    .build();

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(this::write);
      Optional<S2iConfig> config = session.configurators().get(S2iConfig.class);
      if (config.orElse(DEFAULT_SOURCE_TO_IMAGE_CONFIG).isAutoDeployEnabled()) {
        JavaBuildHook hook = new JavaBuildHook(project);
        hook.register();
      }
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurators().add(config(mainClass));
        session.handlers().add(new SourceToImageHandler(session.resources()));
      }
    }
    return false;
  }

  public ConfigurationSupplier<S2iConfig> config(Element mainClass) {
    return new ConfigurationSupplier<>(configurationBuilder(mainClass));
  }

  /**
   * Get or newBuilder a new config for the specified {@link Element}.
   * @param mainClass     The type element of the annotated class (Main).
   * @return              A new config.
   */
  public S2iConfigBuilder configurationBuilder(Element mainClass) {
    EnableS2iBuild enableS2iBuild = mainClass.getAnnotation(EnableS2iBuild.class);
    OpenshiftApplication openshiftApplication = mainClass.getAnnotation(OpenshiftApplication.class);
    OpenshiftConfig openshiftConfig = OpenshiftConfigCustomAdapter.newBuilder(project, openshiftApplication).build();
    return S2iConfigAdapter.newBuilder(enableS2iBuild)
      .accept(new ApplySourceToImageHook())
      .accept(new ApplyOpenshiftConfig(openshiftConfig));
  }
}
