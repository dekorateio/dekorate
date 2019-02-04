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
package io.ap4k.kubernetes.generator;

import io.ap4k.WithProject;
import io.ap4k.Generator;
import io.ap4k.config.ConfigurationSupplier;
import io.ap4k.deps.kubernetes.api.model.KubernetesList;
import io.ap4k.deps.kubernetes.client.DefaultKubernetesClient;
import io.ap4k.deps.kubernetes.client.KubernetesClient;
import io.ap4k.kubernetes.adapter.KubernetesConfigAdapter;
import io.ap4k.kubernetes.annotation.KubernetesApplication;
import io.ap4k.kubernetes.config.KubernetesConfig;
import io.ap4k.kubernetes.configurator.ApplyAutoBuild;
import io.ap4k.kubernetes.handler.KubernetesHandler;
import io.ap4k.project.ApplyProjectInfo;

import javax.lang.model.element.Element;
import java.util.Map;

public interface KubernetesApplicationGenerator extends Generator, WithProject {

  String KUBERNETES = "kubernetes";

  default void add(Map map) {
        add(new ConfigurationSupplier<>(
            KubernetesConfigAdapter
            .newBuilder(propertiesMap(map, KubernetesApplication.class))
            .accept(new ApplyAutoBuild())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(Element element) {
    KubernetesApplication application = element.getAnnotation(KubernetesApplication.class);
     add(new ConfigurationSupplier<>(
            KubernetesConfigAdapter
            .newBuilder(application)
            .accept(new ApplyAutoBuild())
            .accept(new ApplyProjectInfo(getProject()))));
  }

  default void add(ConfigurationSupplier<KubernetesConfig> config)  {
    session.configurators().add(config);
    session.handlers().add(new KubernetesHandler(session.resources()));

    Boolean autoDeployEnabled = session.configurators().get(KubernetesConfig.class).map(c->c.isAutoDeployEnabled()).orElse(false);
    if (autoDeployEnabled) {
      deploy();
    }
  }

  default void deploy() {
    KubernetesList generated = session.generate().getOrDefault(KUBERNETES, new KubernetesList());
    KubernetesClient client = new DefaultKubernetesClient();
    client.resourceList(generated).createOrReplace();
  }
}
