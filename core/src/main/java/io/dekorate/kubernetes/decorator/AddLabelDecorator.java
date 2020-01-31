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

import io.dekorate.kubernetes.config.Label;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.ObjectMetaBuilder;
import io.dekorate.doc.Description;

import java.util.Objects;

/**
 * A decorator that adds a label to resources.
 */
@Description("Add a label to the all metadata.")
public class AddLabelDecorator extends NamedResourceDecorator<ObjectMetaBuilder> {

  private final Label label;

  public AddLabelDecorator(Label label) {
    this(ANY, label);
  }

  public AddLabelDecorator(String name, Label label) {
    super(name);
    this.label = label;
  }

  @Override
  public void andThenVisit(ObjectMetaBuilder builder, ObjectMeta resourceMeta) {
    builder.addToLabels(label.getKey(), label.getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddLabelDecorator addLabelDecorator = (AddLabelDecorator) o;
    return Objects.equals(label, addLabelDecorator.label);
  }

  @Override
  public int hashCode() {

    return Objects.hash(label);
  }
}
