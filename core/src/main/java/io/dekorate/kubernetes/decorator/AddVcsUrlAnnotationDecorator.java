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

import io.dekorate.utils.Annotations;
import io.dekorate.WithProject;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.kubernetes.api.model.ObjectMetaBuilder;
import io.dekorate.doc.Description;
import io.dekorate.project.Project;

/**
 * A decorator that adds a label to resources.
 */
@Description("Add a vcs url label to the all metadata.")
public class AddVcsUrlAnnotationDecorator extends NamedResourceDecorator<ObjectMetaBuilder> implements WithProject {

  private final String annotationKey;

  public AddVcsUrlAnnotationDecorator() {
    this(ANY);
  }

  public AddVcsUrlAnnotationDecorator(String name) {
    this(name, Annotations.VCS_URL);
  }

  public AddVcsUrlAnnotationDecorator(String name, String annotationKey) {
    super(name);
    this.annotationKey = annotationKey;
  }

  @Override
  public void andThenVisit(ObjectMetaBuilder builder, ObjectMeta resourceMeta) {
    Project p = getProject();
    String vcsUri = p.getScmInfo() != null && p.getScmInfo().getUrl() != null ? getProject().getScmInfo().getUrl()
        : Annotations.UNKNOWN;

    builder.addToAnnotations(annotationKey, vcsUri);
  }

  public String getAnnotationKey() {
    return annotationKey;
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[]{ RemoveLabelDecorator.class };
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
