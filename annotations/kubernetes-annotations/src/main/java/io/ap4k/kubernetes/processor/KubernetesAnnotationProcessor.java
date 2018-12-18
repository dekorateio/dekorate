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

package io.ap4k.kubernetes.processor;

import io.ap4k.Session;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.client.DefaultKubernetesClient;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.kubernetes.adapter.KubernetesConfigAdapter;
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.kubernetes.config.ConfigurationSupplier;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.kubernetes.configurator.ApplyAutoBuild;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;
import io.ap4k.project.ApplyProjectInfo;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@Description("Generates kubernetes manifests.")
@SupportedAnnotationTypes("io.ap4k.kubernetes.annotation.KubernetesApplication")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class KubernetesAnnotationProcessor extends AbstractAnnotationProcessor<KubernetesConfig> {

  private final String KUBERNETES = "kubernetes";

  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = Session.getSession();
    if (roundEnv.processingOver()) {
      session.onClose(this::write);
      return true;
    }
    Boolean autoDeployEnabled = session.configurators().get(KubernetesConfig.class).map(c->c.isAutoDeployEnabled()).orElse(false);
    if (autoDeployEnabled) {
      deploy(session);
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurators().add(config(mainClass));
        session.handlers().add(new KubernetesHandler(session.resources()));
      }
    }
    return false;
  }

  /**
   * Get or create a new config for the specified {@link Element}.
   * @param mainClass     The type element of the annotated class (Main).
   * @return              A new config.
   */
  public ConfigurationSupplier<KubernetesConfig> config(Element mainClass) {
    KubernetesApplication application = mainClass.getAnnotation(KubernetesApplication.class);
    return new ConfigurationSupplier<>(
            KubernetesConfigAdapter
            .newBuilder(application)
            .accept(new ApplyAutoBuild())
            .accept(new ApplyProjectInfo(project)));
  }

  public void deploy(Session session) {
    KubernetesList generated = session.generate().getOrDefault(KUBERNETES, new KubernetesList());
    KubernetesClient client = new DefaultKubernetesClient();
    client.resourceList(generated).createOrReplace();
  }
}
