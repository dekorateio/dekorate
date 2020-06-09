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
package io.dekorate.kubernetes.decorator;

import io.dekorate.kubernetes.config.AwsElasticBlockStoreVolume;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.dekorate.doc.Description;

@Description("Add an elastic block store volume to the pod spec.")
public class AddAwsElasticBlockStoreVolumeDecorator extends Decorator<PodSpecBuilder> {

  private final AwsElasticBlockStoreVolume volume;

  public AddAwsElasticBlockStoreVolumeDecorator(AwsElasticBlockStoreVolume volume) {
    this.volume = volume;
  }

  @Override
  public void visit(PodSpecBuilder podSpec) {
    podSpec.addNewVolume()
      .withName(volume.getVolumeName())
      .withNewAwsElasticBlockStore()
      .withVolumeID(volume.getVolumeId())
      .withFsType(volume.getFsType())
      .withPartition(volume.getPartition())
      .withReadOnly(volume.isReadOnly())
      .endAwsElasticBlockStore()
      .endVolume();
  }
}
