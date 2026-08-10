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

package io.dekorate.knative.decorator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.dekorate.kubernetes.config.NodeSelector;
import io.dekorate.kubernetes.decorator.NamedResourceDecorator;
import io.dekorate.utils.Strings;
import io.fabric8.knative.serving.v1.RevisionSpecFluent;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class AddNodeSelectorToRevisionDecorator extends NamedResourceDecorator<RevisionSpecFluent<?>> {

  private final NodeSelector nodeSelector;

  public AddNodeSelectorToRevisionDecorator(NodeSelector nodeSelector) {
    this(ANY, nodeSelector);
  }

  public AddNodeSelectorToRevisionDecorator(String deploymentName, NodeSelector nodeSelector) {
    super(deploymentName);
    this.nodeSelector = nodeSelector;
  }

  public void andThenVisit(RevisionSpecFluent<?> revisionSpec, ObjectMeta resourceMeta) {
    if (Strings.isNotNullOrEmpty(nodeSelector.getKey()) && Strings.isNotNullOrEmpty(nodeSelector.getValue())) {
      Map<String, String> existing = revisionSpec.getNodeSelector();

      if (existing == null)
        existing = new HashMap<>();
      else
        existing.clear();

      existing.put(nodeSelector.getKey(), nodeSelector.getValue());
      revisionSpec.withNodeSelector(existing);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AddNodeSelectorToRevisionDecorator that = (AddNodeSelectorToRevisionDecorator) o;
    return Objects.equals(nodeSelector, that.nodeSelector);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeSelector);
  }
}
