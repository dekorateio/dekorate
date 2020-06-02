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

package io.dekorate.docker.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.Session;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.docker.adapter.DockerBuildConfigAdapter;
import io.dekorate.docker.annotation.DockerBuild;
import io.dekorate.docker.config.DockerBuildConfig;
import io.dekorate.docker.config.DockerBuildConfigBuilder;
import io.dekorate.docker.generator.DockerBuildGenerator;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.project.ApplyProjectInfo;

@SupportedAnnotationTypes({"io.dekorate.docker.annotation.DockerBuild"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DockerAnnotationProcessor extends AbstractAnnotationProcessor implements DockerBuildGenerator {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Session session = getSession();
    if  (roundEnv.processingOver()) {
      session.close();
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
    DockerBuild enableDockerBuild = element.getAnnotation(DockerBuild.class);
    on(enableDockerBuild != null
      ? new AnnotationConfiguration<DockerBuildConfig>(DockerBuildConfigAdapter.newBuilder(enableDockerBuild)
                                                                                 .accept(new ApplyProjectInfo(getProject()))
                                                                                 .accept(new ApplyBuildToImageConfiguration()))
      : new AnnotationConfiguration<DockerBuildConfig>(new DockerBuildConfigBuilder()
                                                                                 .accept(new ApplyProjectInfo(getProject()))
                                                                                 .accept(new ApplyBuildToImageConfiguration())));
  }
}
