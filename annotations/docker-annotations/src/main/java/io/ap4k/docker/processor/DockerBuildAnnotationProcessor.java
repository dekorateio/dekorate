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
package io.ap4k.docker.processor;

import io.ap4k.Session;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.docker.adapter.DockerBuildConfigAdapter;
import io.ap4k.docker.annotation.DockerBuild;
import io.ap4k.docker.config.DockerBuildConfig;
import io.ap4k.docker.hook.DockerBuildHook;
import io.ap4k.docker.configurator.ApplyDockerBuildHook;
import io.ap4k.docker.configurator.ApplyProjectInfoToDockerBuildConfig;
import io.ap4k.processor.AbstractAnnotationProcessor;
import io.ap4k.doc.Description;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

@Description("Register a docker build hook.")
@SupportedAnnotationTypes("io.ap4k.docker.annotation.DockerBuild")
public class DockerBuildAnnotationProcessor extends AbstractAnnotationProcessor<DockerBuildConfig> {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Session session = Session.getSession();
    if  (roundEnv.processingOver()) {
      session.onClose(r -> write(r));
      Optional<DockerBuildConfig> config = session.configurators().get(DockerBuildConfig.class);
      if (config.isPresent() && config.get().isAutoBuildEnabled()) {
        DockerBuildHook hook = new DockerBuildHook(project, config.get());
        hook.register();
      }
      return true;
    }
    for (TypeElement typeElement : annotations) {
      for (Element mainClass : roundEnv.getElementsAnnotatedWith(typeElement)) {
        session.configurators().add(config(mainClass));
      }
    }
    return false;
  }

  @Override
  public ConfigurationSupplier<DockerBuildConfig> config(Element mainClass) {
    DockerBuild dockerBuild = mainClass.getAnnotation(DockerBuild.class);
    return new ConfigurationSupplier<DockerBuildConfig>(DockerBuildConfigAdapter
                                                        .newBuilder(dockerBuild)
                                                        .accept(new ApplyProjectInfoToDockerBuildConfig(project))
                                                        .accept(new ApplyDockerBuildHook()));
  }

}
