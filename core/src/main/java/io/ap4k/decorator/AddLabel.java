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

package io.ap4k.decorator;

import io.ap4k.config.Label;
import io.ap4k.deps.kubernetes.api.model.ObjectMetaBuilder;

import java.util.Objects;

/**
 * A decorator that adds a label to resources.
 */
public class AddLabel extends Decorator<ObjectMetaBuilder> {

  private final Label label;

  public AddLabel(Label label) {
    this.label = label;
  }

  @Override
  public void visit(ObjectMetaBuilder builder) {
    builder.addToLabels(label.getKey(), label.getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddLabel addLabel = (AddLabel) o;
    return Objects.equals(label, addLabel.label);
  }

  @Override
  public int hashCode() {

    return Objects.hash(label);
  }
}
