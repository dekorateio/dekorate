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

package io.dekorate.s2i.apt;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.kubernetes.configurator.ApplyBuildToImageConfiguration;
import io.dekorate.processor.AbstractAnnotationProcessor;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.s2i.adapter.S2iBuildConfigAdapter;
import io.dekorate.s2i.annotation.S2iBuild;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.s2i.config.S2iBuildConfigBuilder;
import io.dekorate.s2i.generator.S2iBuildGenerator;

@SupportedAnnotationTypes({"io.dekorate.s2i.annotation.S2iBuild"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class S2iAnnotationProcessor extends AbstractAnnotationProcessor implements S2iBuildGenerator {

	@Override
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
    S2iBuild enableS2iBuild = element.getAnnotation(S2iBuild.class);
    on(enableS2iBuild != null
       ? new AnnotationConfiguration<S2iBuildConfig>(S2iBuildConfigAdapter.newBuilder(enableS2iBuild)
                                                     .accept(new ApplyBuildToImageConfiguration())
                                                     .accept(new ApplyProjectInfo(getProject())))
       : new AnnotationConfiguration<S2iBuildConfig>(new S2iBuildConfigBuilder()
                                                     .accept(new ApplyBuildToImageConfiguration())
                                                     .accept(new ApplyProjectInfo(getProject()))));
  }
}
