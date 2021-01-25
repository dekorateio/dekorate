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
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

/**
 * A decorator that removes an annotation from the matching resources (filtered by name and/or kinds).
 */
@Description("Remove an annotation from the matching resources (filtered by name and/or kinds).")
public class RemoveAnnotationDecorator extends NamedResourceDecorator<ObjectMetaBuilder> {

  private final String annotationKey;

  public RemoveAnnotationDecorator(String annotationKey) {
    this(ANY, annotationKey);
  }

  public RemoveAnnotationDecorator(String name, String annotationKey) {
    super(name);
    this.annotationKey = annotationKey;
  }

  @Override
  public void andThenVisit(ObjectMetaBuilder builder, ObjectMeta resourceMeta) {
    builder.removeFromAnnotations(annotationKey);
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddAnnotationDecorator.class, AddVcsUrlAnnotationDecorator.class,
        AddCommitIdAnnotationDecorator.class };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((annotationKey == null) ? 0 : annotationKey.hashCode());
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
    RemoveAnnotationDecorator other = (RemoveAnnotationDecorator) obj;
    if (annotationKey == null) {
      if (other.annotationKey != null)
        return false;
    } else if (!annotationKey.equals(other.annotationKey))
      return false;
    return true;
  }

}
