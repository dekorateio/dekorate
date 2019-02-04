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
import io.ap4k.openshift.adapter.S2iConfigAdapter;
import io.ap4k.openshift.annotation.EnableS2iBuild;
import io.ap4k.openshift.annotation.OpenshiftApplication;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.OpenshiftConfigCustomAdapter;
import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.openshift.config.S2iConfigBuilder;
import io.ap4k.openshift.configurator.ApplyOpenshiftConfig;
import io.ap4k.openshift.configurator.ApplySourceToImageHook;
import io.ap4k.openshift.handler.SourceToImageHandler;
import io.ap4k.openshift.hook.OcBuildHook;

import javax.lang.model.element.Element;
import java.nio.file.Path;
import java.util.Map;

public interface S2iBuildGenerator extends Generator, WithSession, WithProject {

  String DEFAULT_S2I_BUILDER_IMAGE = "fabric8/s2i-java:2.3";

  S2iConfig DEFAULT_SOURCE_TO_IMAGE_CONFIG = new S2iConfigBuilder()
    .withBuilderImage(DEFAULT_S2I_BUILDER_IMAGE)
    .build();

  default void add(Map map) {
    OpenshiftConfig openshiftConfig = OpenshiftConfigAdapter.newBuilder((Map) map.get(OpenshiftApplication.class.getName())).build();
    on(new ConfigurationSupplier<>(S2iConfigAdapter.newBuilder(propertiesMap(map, EnableS2iBuild.class))
      .accept(new ApplySourceToImageHook(openshiftConfig))
      .accept(new ApplyOpenshiftConfig(openshiftConfig))));
  }

  default void add(Element mainClass) {
    EnableS2iBuild enableS2iBuild = mainClass.getAnnotation(EnableS2iBuild.class);
    OpenshiftApplication openshiftApplication = mainClass.getAnnotation(OpenshiftApplication.class);
    OpenshiftConfig openshiftConfig = OpenshiftConfigCustomAdapter.newBuilder(getProject(), openshiftApplication).build();

    on(new ConfigurationSupplier<>(S2iConfigAdapter.newBuilder(enableS2iBuild)
      .accept(new ApplySourceToImageHook(openshiftConfig))
      .accept(new ApplyOpenshiftConfig(openshiftConfig))));
  }
  default void on(ConfigurationSupplier<S2iConfig> config) {
    session.configurators().add(config);
    session.handlers().add(new SourceToImageHandler(session.resources()));
  }

  default void onClosed() {
    S2iConfig s2iConfig = session.configurators().get(S2iConfig.class).orElse(DEFAULT_SOURCE_TO_IMAGE_CONFIG);
    if (s2iConfig.isAutoBuildEnabled() || s2iConfig.isAutoDeployEnabled()) {
      OcBuildHook hook = new OcBuildHook(s2iConfig, getProject(), getOutputDirectory());
      hook.register();
    }
  }

  Path getOutputDirectory();
}
