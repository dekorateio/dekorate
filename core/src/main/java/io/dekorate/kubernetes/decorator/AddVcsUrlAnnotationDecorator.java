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
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

/**
 * A decorator that adds a label to resources.
 */
@Description("Add a vcs url label to the all metadata.")
public class AddVcsUrlAnnotationDecorator extends NamedResourceDecorator<ObjectMetaBuilder> {

  private final String annotationKey;
  private final String url;

  public AddVcsUrlAnnotationDecorator(String name, String annotationKey, String url) {
    super(name);
    this.url = url;
    this.annotationKey = annotationKey;
  }

  @Override
  public void andThenVisit(ObjectMetaBuilder builder, ObjectMeta resourceMeta) {
    if (Strings.isNotNullOrEmpty(url)) {
      builder.addToAnnotations(annotationKey, url);
    }
  }

  public String getAnnotationKey() {
    return annotationKey;
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { RemoveLabelDecorator.class };
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1 + getClass().hashCode();
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
    AddVcsUrlAnnotationDecorator other = (AddVcsUrlAnnotationDecorator) obj;
    if (annotationKey == null) {
      if (other.annotationKey != null)
        return false;
    } else if (!annotationKey.equals(other.annotationKey))
      return false;
    return true;
  }

}
