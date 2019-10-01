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

package io.dekorate.docker.generator;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.lang.model.element.Element;

import io.dekorate.Generator;
import io.dekorate.Session;
import io.dekorate.WithSession;
import io.dekorate.config.AnnotationConfiguration;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.config.PropertyConfiguration;
import io.dekorate.docker.adapter.*;
import io.dekorate.docker.annotation.DockerBuild;
import io.dekorate.docker.config.*;

public interface DockerBuildGenerator extends Generator, WithSession {

  default String getKey() {
    return "docker";
  }

  default Class<? extends Annotation> getAnnotation() {
    return DockerBuild.class;
  }


  @Override
  default void add(Map map) {
    on(new PropertyConfiguration<DockerBuildConfig>(DockerBuildConfigAdapter.newBuilder(propertiesMap(map, DockerBuild.class))));
  }

  @Override
  default void add(Element element) {
    DockerBuild enableDockerBuild = element.getAnnotation(DockerBuild.class);
    on(enableDockerBuild != null
      ? new AnnotationConfiguration<DockerBuildConfig>(DockerBuildConfigAdapter.newBuilder(enableDockerBuild))
      : new AnnotationConfiguration<DockerBuildConfig>(new DockerBuildConfigBuilder()));
  }

  default void on(ConfigurationSupplier<DockerBuildConfig> config) {
    Session session = getSession();
    session.configurators().add(config);
  }
}
