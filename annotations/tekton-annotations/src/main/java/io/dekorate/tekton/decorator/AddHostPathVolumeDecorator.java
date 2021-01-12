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
import java.util.function.Predicate;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.tekton.pipeline.v1beta1.TaskSpecFluent;

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
      Predicate<VolumeBuilder> predicate = matchingVolume(name);
      if (spec.hasMatchingVolume(predicate)) {
          spec.removeMatchingFromVolumes(predicate);
      }

      spec.addToVolumes(new VolumeBuilder()
                      .withName(name)
                      .withNewHostPath()
                        .withType(type)
                        .withPath(path)
                      .endHostPath()
                      .build());
  }

    private static Predicate<VolumeBuilder> matchingVolume(String name) {
        return new Predicate<VolumeBuilder>() {
            @Override
            public boolean test(VolumeBuilder volume) {
                return Strings.isNullOrEmpty(name) || volume.getName().equals(name);
            }
        };
    }
}
