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
package io.ap4k.openshift.decorator;

import io.ap4k.deps.kubernetes.api.builder.Predicate;
import io.ap4k.deps.openshift.api.model.DeploymentConfigSpecFluent;
import io.ap4k.deps.openshift.api.model.DeploymentTriggerPolicyBuilder;
import io.ap4k.kubernetes.decorator.AddAwsElasticBlockStoreVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddAzureDiskVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddEnvVarDecorator;
import io.ap4k.kubernetes.decorator.AddInitContainerDecorator;
import io.ap4k.kubernetes.decorator.AddMountDecorator;
import io.ap4k.kubernetes.decorator.AddPortDecorator;
import io.ap4k.kubernetes.decorator.AddPvcVolumeDecorator;
import io.ap4k.kubernetes.decorator.AddReadinessProbeDecorator;
import io.ap4k.kubernetes.decorator.AddSidecarDecorator;
import io.ap4k.kubernetes.decorator.ApplyArgsDecorator;
import io.ap4k.kubernetes.decorator.ApplyCommandDecorator;
import io.ap4k.kubernetes.decorator.ApplyImageDecorator;
import io.ap4k.kubernetes.decorator.ApplyImagePullPolicyDecorator;
import io.ap4k.kubernetes.decorator.ApplyServiceAccountDecorator;
import io.ap4k.kubernetes.decorator.ApplyWorkingDirDecorator;
import io.ap4k.kubernetes.decorator.Decorator;

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
      AddReadinessProbeDecorator.class,
      AddSidecarDecorator.class,
      AddInitContainerDecorator.class};
  }
}
