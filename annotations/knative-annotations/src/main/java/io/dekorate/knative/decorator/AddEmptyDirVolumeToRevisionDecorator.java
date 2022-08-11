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
package io.dekorate.knative.decorator;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.EmptyDirVolume;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.fabric8.knative.serving.v1.RevisionSpecFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

@Description("Add an emptyDir volume to the pod spec.")
public class AddEmptyDirVolumeToRevisionDecorator extends NamedResourceDecorator<RevisionSpecFluent<?>> {

  private final EmptyDirVolume volume;

  public AddEmptyDirVolumeToRevisionDecorator(EmptyDirVolume volume) {
    this(ANY, volume);
  }

  public AddEmptyDirVolumeToRevisionDecorator(String name, EmptyDirVolume volume) {
    super(name);
    this.volume = volume;
  }

  @Override
  public void andThenVisit(RevisionSpecFluent<?> revisionSpec, ObjectMeta resourceMeta) {
    revisionSpec.addNewVolume()
        .withName(volume.getVolumeName())
        .withNewEmptyDir()
        .endEmptyDir()
        .endVolume();

  }
}
