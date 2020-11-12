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
package io.dekorate.tekton.decorator;

import io.dekorate.doc.Description;
import io.dekorate.utils.Strings;
import io.dekorate.deps.kubernetes.api.builder.Predicate;
import io.dekorate.deps.kubernetes.api.model.Volume;
import io.dekorate.deps.kubernetes.api.model.VolumeBuilder;
import io.dekorate.deps.tekton.pipeline.v1beta1.TaskSpecFluent;

@Description("Add a persistent host path volume to the specified task.")
public class AddHostPathVolumeDecorator extends NamedTaskDecorator {

    private final String name;
    private final String path;
    private final String type;

    public AddHostPathVolumeDecorator(String taskName, String name, String path, String type) {
        super(taskName);
        this.name = name;
        this.path = path;
        this.type = type;
    }

  @Override
  public void andThenVisit(TaskSpecFluent<?> spec) {
      Predicate<Volume> predicate = matchingVolume(name);
      if (spec.hasMatchingVolume(predicate)) {
          Volume toRemove = spec.getMatchingVolume(predicate);
          spec.removeFromVolumes(toRemove);
      }

      spec.addToVolumes(new VolumeBuilder()
                      .withName(name)
                      .withNewHostPath()
                        .withType(type)
                        .withPath(path)
                      .endHostPath()
                      .build());
  }

    private static Predicate<Volume> matchingVolume(String name) {
        return new Predicate<Volume>() {

            @Override
            public Boolean apply(Volume volume) {
                return Strings.isNullOrEmpty(name) || volume.getName().equals(name);
            }
        };
    }
}
