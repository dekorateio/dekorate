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
package io.dekorate.openshift.decorator;

import io.dekorate.deps.kubernetes.api.builder.Predicate;
import io.dekorate.deps.openshift.api.model.DeploymentConfigSpecFluent;
import io.dekorate.deps.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.dekorate.kubernetes.decorator.AddAwsElasticBlockStoreVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddAzureDiskVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddEnvVarDecorator;
import io.dekorate.kubernetes.decorator.AddInitContainerDecorator;
import io.dekorate.kubernetes.decorator.AddLivenessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddMountDecorator;
import io.dekorate.kubernetes.decorator.AddPortDecorator;
import io.dekorate.kubernetes.decorator.AddPvcVolumeDecorator;
import io.dekorate.kubernetes.decorator.AddReadinessProbeDecorator;
import io.dekorate.kubernetes.decorator.AddSidecarDecorator;
import io.dekorate.kubernetes.decorator.ApplyArgsDecorator;
import io.dekorate.kubernetes.decorator.ApplyCommandDecorator;
import io.dekorate.kubernetes.decorator.ApplyImageDecorator;
import io.dekorate.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.dekorate.kubernetes.decorator.ApplyServiceAccountDecorator;
import io.dekorate.kubernetes.decorator.ApplyWorkingDirDecorator;
import io.dekorate.kubernetes.decorator.Decorator;

public class ApplyDeploymentTriggerDecorator extends Decorator<DeploymentConfigSpecFluent<?>> {

  private static final String IMAGESTREAMTAG = "ImageStreamTag";
  private static final String IMAGECHANGE = "ImageChange";

  private final String containerName;
  private final String imageStreamTag;
  private final Predicate<DeploymentTriggerPolicyBuilder> predicate;

  public ApplyDeploymentTriggerDecorator(String containerName, String imageStreamTag) {
    this.containerName = containerName;
    this.imageStreamTag = imageStreamTag;
    this.predicate  = d -> d.hasImageChangeParams() && d.buildImageChangeParams().getContainerNames() != null && d.buildImageChangeParams().getContainerNames().contains(containerName);
  }

  @Override
  public void visit(DeploymentConfigSpecFluent<?> deploymentConfigSpec) {
    DeploymentConfigSpecFluent.TriggersNested<?> target;

    if (deploymentConfigSpec.buildMatchingTrigger(predicate) != null)  {
      target = deploymentConfigSpec.editMatchingTrigger(predicate);
    } else {
      target = deploymentConfigSpec.addNewTrigger();
    }
    target.withType(IMAGECHANGE)
      .withNewImageChangeParams()
      .withAutomatic(true)
      .withContainerNames(containerName)
      .withNewFrom()
      .withKind(IMAGESTREAMTAG)
      .withName(imageStreamTag)
      .endFrom()
      .endImageChangeParams()
      .endTrigger();
  }

  @Override
  public Class<? extends Decorator>[] after() {
    //Due to: https://github.com/sundrio/sundrio/issues/135 this decorator breaks the decoratros below.
    //So, let's make sure its called after them (as a workaround).
    return new Class[] {AddEnvVarDecorator.class, AddPortDecorator.class,
      AddMountDecorator.class, AddPvcVolumeDecorator.class, AddAwsElasticBlockStoreVolumeDecorator.class, AddAzureDiskVolumeDecorator.class, AddAwsElasticBlockStoreVolumeDecorator.class,
      ApplyImageDecorator.class, ApplyImagePullPolicyDecorator.class,
      ApplyWorkingDirDecorator.class, ApplyCommandDecorator.class, ApplyArgsDecorator.class,
      ApplyServiceAccountDecorator.class,
      AddReadinessProbeDecorator.class,
      AddLivenessProbeDecorator.class,
      AddSidecarDecorator.class,
      AddInitContainerDecorator.class};
  }
}
