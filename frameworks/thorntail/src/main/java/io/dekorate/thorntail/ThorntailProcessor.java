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
package io.dekorate.thorntail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.ConfigurationRegistry;
import io.dekorate.Session;
import io.dekorate.config.AdditionalResourcesLocator;
import io.dekorate.doc.Description;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.thorntail.configurator.ThorntailPrometheusAgentConfigurator;

@Description("Detects JAX-RS and servlet annotations and registers the http port.")
@SupportedAnnotationTypes({ "javax.ws.rs.ApplicationPath", "javax.ws.rs.Path", "javax.servlet.annotation.WebServlet" })
public class ThorntailProcessor extends AbstractAnnotationProcessor implements ThorntailWebAnnotationGenerator {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = getSession();
    session.getConfigurationRegistry().add(new ThorntailPrometheusAgentConfigurator());

    if (roundEnv.processingOver()) {
      session.close();
      return true;
    }

    List<String> resourceNames = new ArrayList<>();
    // default resource names:
    resourceNames.add("project-defaults.yml");
    // resource names from active Dekorate features
    resourceNames.addAll(AdditionalResourcesLocator.getAdditionalResources());

    session.addPropertyConfiguration(readApplicationConfig(resourceNames.toArray(new String[resourceNames.size()])));
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        add(mainClass);
      }
    }
    return false;
  }

  @Override
  public ConfigurationRegistry getConfigurationRegistry() {
    return Session.getSession().getConfigurationRegistry();
  }
}
