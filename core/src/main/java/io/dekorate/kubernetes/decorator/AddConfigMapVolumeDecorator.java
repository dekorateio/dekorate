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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.ConfigMapVolume;
import io.dekorate.kubernetes.config.Item;
import io.fabric8.kubernetes.api.model.KeyToPath;
import io.fabric8.kubernetes.api.model.KeyToPathBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpecFluent;

@Description("Add a configmap volume to the pod spec.")
public class AddConfigMapVolumeDecorator extends NamedResourceDecorator<PodSpecFluent<?>> {

  private final ConfigMapVolume volume;

  public AddConfigMapVolumeDecorator(ConfigMapVolume volume) {
    this(ANY, volume);
  }

  public AddConfigMapVolumeDecorator(String name, ConfigMapVolume volume) {
    super(name);
    this.volume = volume;
  }

  @Override
  public void andThenVisit(PodSpecFluent<?> podSpec, ObjectMeta resourceMeta) {
    podSpec.addNewVolume()
        .withName(volume.getVolumeName())
        .withNewConfigMap()
        .withName(volume.getConfigMapName())
        .withDefaultMode(volume.getDefaultMode())
        .withOptional(volume.isOptional())
        .withItems(toKeyToPathList(volume.getItems()))
        .endConfigMap()
        .endVolume();
  }

  private List<KeyToPath> toKeyToPathList(Item[] items) {
    if (items == null || items.length == 0) {
      return Collections.emptyList();
    }

    List<KeyToPath> keyToPathList = new ArrayList<>(items.length);
    for (Item item : items) {
      KeyToPathBuilder builder = new KeyToPathBuilder()
          .withKey(item.getKey())
          .withPath(item.getPath());
      if (item.getMode() > 0) {
        builder.withMode(item.getMode());
      }

      keyToPathList.add(builder.build());
    }

    return keyToPathList;
  }
}
