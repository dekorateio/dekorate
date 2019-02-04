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

package io.ap4k.openshift.generator;

import io.ap4k.WithProject;
import io.ap4k.WithSession;
import io.ap4k.Generator;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.openshift.adapter.OpenshiftConfigAdapter;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.openshift.config.OpenshiftConfig;

import javax.lang.model.element.Element;

import io.ap4k.openshift.handler.OpenshiftHandler;
import io.ap4k.project.ApplyProjectInfo;

import java.util.Map;

public interface OpenshiftApplicationGenerator extends Generator, WithSession, WithProject {

  default void add(Element element) {
    on(new ConfigurationSupplier<>(OpenshiftConfigAdapter.newBuilder(element.getAnnotation(OpenshiftApplication.class))
        .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Map map) {
    on(new ConfigurationSupplier<>(OpenshiftConfigAdapter.newBuilder(propertiesMap(map, OpenshiftApplication.class))
        .accept(new ApplyProjectInfo(getProject()))));
  }

    default void on(ConfigurationSupplier<OpenshiftConfig> config) {
      session.configurators().add(config);
      session.handlers().add(new OpenshiftHandler(session.resources()));
  }

}
