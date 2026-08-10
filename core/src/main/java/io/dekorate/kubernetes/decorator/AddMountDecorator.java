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
import io.dekorate.kubernetes.config.Mount;
import io.fabric8.kubernetes.api.model.ContainerBuilder;

@Description("Add mount to the specified container.")
public class AddMountDecorator extends ApplicationContainerDecorator<ContainerBuilder> {

  private final Mount mount;

  public AddMountDecorator(Mount mount) {
    this(ANY, ANY, mount);
  }

  public AddMountDecorator(String deployment, String container, Mount mount) {
    super(deployment, container);
    this.mount = mount;
  }

  @Override
  public void andThenVisit(ContainerBuilder container) {
    container.removeMatchingFromVolumeMounts(m -> mount.getName().equals(m.getName()));

    container.addNewVolumeMount()
        .withName(mount.getName())
        .withMountPath(mount.getPath())
        .withSubPath(mount.getSubPath())
        .withReadOnly(mount.isReadOnly())
        .endVolumeMount();
  }
}
