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

import java.util.Arrays;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Label;
import io.dekorate.utils.Metadata;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;

/**
 * A decorator that adds a label to the matching resources (filtered by name and/or kinds).
 */
@Description("Add a label to the matching resources (filtered by name and/or kinds).")
public class AddLabelDecorator extends NamedResourceDecorator<VisitableBuilder> {

  private final Label label;

  public AddLabelDecorator(Label label) {
    this(ANY, label);
  }

  public AddLabelDecorator(String name, Label label) {
    super(ANY, name);
    this.label = label;
  }

  public AddLabelDecorator(String name, String key, String value, String... kinds) {
    super(ANY, name);
    this.label = new Label(key, value, kinds);
  }


  @Override
  public void andThenVisit(VisitableBuilder builder, String kind, ObjectMeta resourceMeta) {
    if (label.getKinds() == null || label.getKinds().length == 0 || Arrays.asList(label.getKinds()).contains(kind)) {
      andThenVisit(builder, resourceMeta);
    }
  }

  @Override
  public void andThenVisit(VisitableBuilder builder, ObjectMeta resourceMeta) {
    Metadata.addToLabels(builder, label.getKey(), label.getValue());
  }

  public Label getLabel() {
    return label;
  }

  public String getLabelKey() {
    return label.getKey();
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { RemoveLabelDecorator.class };
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class, AddSidecarDecorator.class };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1 + getClass().hashCode();
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AddLabelDecorator other = (AddLabelDecorator) obj;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    return true;
  }

}
