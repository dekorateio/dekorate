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

package io.ap4k.openshift.handler;

import io.ap4k.AbstractKubernetesHandler;
import io.ap4k.Resources;
import io.ap4k.deps.kubernetes.api.model.KubernetesListBuilder;
import io.ap4k.deps.kubernetes.api.model.PodSpec;
import io.ap4k.deps.kubernetes.api.model.PodSpecBuilder;
import io.ap4k.deps.kubernetes.api.model.PodTemplateSpec;
import io.ap4k.deps.kubernetes.api.model.PodTemplateSpecBuilder;
import io.ap4k.deps.openshift.api.model.DeploymentConfig;
import io.ap4k.deps.openshift.api.model.DeploymentConfigBuilder;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.openshift.config.OpenshiftConfig;
import io.ap4k.openshift.config.EditableOpenshiftConfig;
import io.ap4k.openshift.decorator.AddRouteDecorator;
import io.ap4k.openshift.decorator.ApplyDeploymentTriggerDecorator;
import io.ap4k.openshift.decorator.ApplyReplicasDecorator;

import java.util.Optional;

import static io.ap4k.utils.Labels.createLabels;

public class OpenshiftHandler extends AbstractKubernetesHandler<OpenshiftConfig> {

  private static final String OPENSHIFT = "openshift";
  private static final String APP = "app";
  private static final String VERSION = "version";

  private static final String IF_NOT_PRESENT = "IfNotPresent";
  private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
  private static final String METADATA_NAMESPACE = "metadata.namespace";

  private static final String IMAGESTREAMTAG = "ImageStreamTag";
  private static final String IMAGECHANGE = "ImageChange";

  private static final String JAVA_APP_JAR = "JAVA_APP_JAR";

  public OpenshiftHandler() {
    super(new Resources());
  }
  public OpenshiftHandler(Resources resources) {
    super(resources);
  }

  @Override
  public int order() {
    return 300;
  }

  public void handle(OpenshiftConfig config) {
    setApplicationInfo(config);
    Optional<DeploymentConfig> existingDeploymentConfig = resources.groups().getOrDefault(OPENSHIFT, new KubernetesListBuilder()).buildItems().stream()
      .filter(i -> i instanceof DeploymentConfig)
      .map(i -> (DeploymentConfig)i)
      .filter(i -> i.getMetadata().getName().equals(config.getName()))
      .findAny();

    if (!existingDeploymentConfig.isPresent()) {
      resources.add(OPENSHIFT, createDeploymentConfig(config));
    }
    addDecorators(OPENSHIFT, config);
  }

  @Override
  protected void addDecorators(String group, OpenshiftConfig config) {
    super.addDecorators(group, config);
    resources.decorate(group, new ApplyReplicasDecorator(config.getReplicas()));
    resources.decorate(group, new ApplyDeploymentTriggerDecorator(config.getName(), config.getName() + ":" + config.getVersion()));
    resources.decorate(group, new AddRouteDecorator(config, resources.getLabels()));
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(OpenshiftConfig.class) ||
      type.equals(EditableOpenshiftConfig.class);
  }

  /**
   * Creates a {@link DeploymentConfig} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The deployment config.
   */
  public DeploymentConfig createDeploymentConfig(OpenshiftConfig config)  {
    return new DeploymentConfigBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withNewReplicas(1)
      .withTemplate(createPodTemplateSpec(config))
      .withSelector(resources.getLabels())
      .addNewTrigger()
      .withType(IMAGECHANGE)
      .withNewImageChangeParams()
      .withAutomatic(true)
      .withContainerNames(config.getName())
      .withNewFrom()
      .withKind(IMAGESTREAMTAG)
      .withName(config.getName() + ":" + config.getVersion())
      .endFrom()
      .endImageChangeParams()
      .endTrigger()
      .endSpec()
      .build();
  }

  /**
   * Creates a {@link PodTemplateSpec} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The pod template specification.
   */
  public PodTemplateSpec createPodTemplateSpec(OpenshiftConfig config) {
    return new PodTemplateSpecBuilder()
      .withSpec(createPodSpec(config))
      .withNewMetadata()
      .withLabels(resources.getLabels())
      .endMetadata()
      .build();
  }

  /**
   * Creates a {@link PodSpec} for the {@link OpenshiftConfig}.
   * @param config   The sesssion.
   * @return          The pod specification.
   */
  public static PodSpec createPodSpec(OpenshiftConfig config) {
    return new PodSpecBuilder()
      .addNewContainer()
      .withName(config.getName())
      .withImage("")
      .withImagePullPolicy(IF_NOT_PRESENT)
      .addNewEnv()
      .withName(KUBERNETES_NAMESPACE)
      .withNewValueFrom()
      .withNewFieldRef(null, METADATA_NAMESPACE)
      .endValueFrom()
      .endEnv()
      .addNewEnv()
      .withName(JAVA_APP_JAR)
      .withValue("/deployments/" + config.getProject().getBuildInfo().getOutputFile().getFileName().toString())
      .endEnv()
      .endContainer()
      .build();
  }
}
