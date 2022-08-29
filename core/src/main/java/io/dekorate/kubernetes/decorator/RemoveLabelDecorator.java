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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaFluent;

/**
 * A decorator that removes a label from the matching resources (filtered by name and/or kinds).
 */
@Description("Remove a label from the matching resources (filtered by name and/or kinds).")
public class RemoveLabelDecorator extends NamedResourceDecorator<ObjectMetaFluent> {

  private final String labelKey;

  public RemoveLabelDecorator(String labelKey) {
    this(ANY, labelKey);
  }

  public RemoveLabelDecorator(String name, String labelKey) {
    super(name);
    this.labelKey = labelKey;
  }

  @Override
  public void andThenVisit(ObjectMetaFluent item, ObjectMeta resourceMeta) {
    item.removeFromLabels(labelKey);

  }

  public String getLabelKey() {
    return labelKey;
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddLabelDecorator.class };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1 + getClass().hashCode();
    result = prime * result + ((labelKey == null) ? 0 : labelKey.hashCode());
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
    RemoveLabelDecorator other = (RemoveLabelDecorator) obj;
    if (labelKey == null) {
      if (other.labelKey != null)
        return false;
    } else if (!labelKey.equals(other.labelKey))
      return false;
    return true;
  }
}
