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

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.SecretVolume;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpecFluent;

@Description("Add a secret volume to all pod specs.")
public class AddSecretVolumeDecorator extends NamedResourceDecorator<PodSpecFluent<?>> {

  private final SecretVolume volume;

  public AddSecretVolumeDecorator(SecretVolume volume) {
    this(ANY, volume);
  }

  public AddSecretVolumeDecorator(String name, SecretVolume volume) {
    super(name);
    this.volume = volume;
  }

  @Override
  public void andThenVisit(PodSpecFluent<?> podSpec, ObjectMeta resourceMeta) {
    podSpec.addNewVolume()
        .withName(volume.getVolumeName())
        .withNewSecret()
        .withSecretName(volume.getSecretName())
        .withDefaultMode(volume.getDefaultMode())
        .withOptional(volume.isOptional())
        .endSecret()
        .endVolume();
  }
}
