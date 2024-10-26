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

package io.dekorate.kubernetes.decorator;

import java.util.Objects;

import io.dekorate.kubernetes.config.NodeSelector;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpecFluent;

public class AddNodeSelectorDecorator extends NamedResourceDecorator<PodSpecFluent<?>> {

  private final NodeSelector nodeSelector;

  public AddNodeSelectorDecorator(String deploymentName, NodeSelector nodeSelector) {
    super(deploymentName);
    this.nodeSelector = nodeSelector;
  }

  public void andThenVisit(PodSpecFluent<?> podSpec, ObjectMeta resourceMeta) {
    if (Strings.isNotNullOrEmpty(nodeSelector.getKey()) && Strings.isNotNullOrEmpty(nodeSelector.getValue())) {
      podSpec.removeFromNodeSelector(nodeSelector.getKey());
      podSpec.addToNodeSelector(nodeSelector.getKey(), nodeSelector.getValue());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AddNodeSelectorDecorator that = (AddNodeSelectorDecorator) o;
    return Objects.equals(nodeSelector, that.nodeSelector);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeSelector);
  }
}
