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
package io.dekorate.knative.handler;

import java.util.Optional;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.knative.serving.v1beta1.*;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Configurator;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.kubernetes.configurator.ApplyAutoBuild;
import io.dekorate.knative.config.EditableKnativeConfig;
import io.dekorate.knative.config.KnativeConfig;
import io.dekorate.knative.config.KnativeConfigBuilder;
import io.dekorate.project.ApplyProjectInfo;
import io.dekorate.project.Project;
import io.dekorate.utils.Images;

public class KnativeHandler extends AbstractKubernetesHandler<KnativeConfig> implements HandlerFactory, WithProject {

  private static final String KNATIVE = "knative";
  private static final String APP = "app";
  private static final String VERSION = "version";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  private static final String JAVA_APP_JAR = "JAVA_APP_JAR";

  public KnativeHandler() {
    super(new Resources());
  }
  public KnativeHandler(Resources resources) {
    super(resources);
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new KnativeHandler(resources);
  }

  @Override
  public int order() {
    return 400;
  }

  public void handle(KnativeConfig config) {
    setApplicationInfo(config);
    Optional<Service> existingService = resources.groups().getOrDefault(KNATIVE, new KubernetesListBuilder()).buildItems().stream()
      .filter(i -> i instanceof Service)
      .map(i -> (Service)i)
      .filter(i -> i.getMetadata().getName().equals(config.getName()))
      .findAny();

    if (!existingService.isPresent()) {
      resources.add(KNATIVE, createService(config));
    }
    addDecorators(KNATIVE, config);
  }

  @Override
  public ConfigurationSupplier<KnativeConfig> getFallbackConfig() {
    Project p = getProject();
    return new ConfigurationSupplier<KnativeConfig>(new KnativeConfigBuilder().accept(new ApplyAutoBuild()).accept(new ApplyProjectInfo(p)));
  }


  @Override
  protected void addDecorators(String group, KnativeConfig config) {
    super.addDecorators(group, config);
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(KnativeConfig.class) ||
      type.equals(EditableKnativeConfig.class);
  }

  /**
   * Creates a {@link Service} for the {@link KnativeConfig}.
   * @param config   The sesssion.
   * @return          The deployment config.
   */
  public Service createService(KnativeConfig config)  {
    return new ServiceBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .endSpec()
      .build();
  }
}
