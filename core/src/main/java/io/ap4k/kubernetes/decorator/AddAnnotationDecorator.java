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

package io.ap4k.kubernetes.decorator;

import io.ap4k.deps.kubernetes.api.model.ObjectMetaBuilder;
import io.ap4k.doc.Description;
import io.ap4k.kubernetes.config.Annotation;

import java.util.Objects;

 @Description("A decorator that adds an annotation to all resources.")
 public class AddAnnotationDecorator extends ApplicationResourceDecorator<ObjectMetaBuilder> {

  private final Annotation annotation;

  public AddAnnotationDecorator(Annotation annotation) {
    this(ANY, annotation);
  }

   public AddAnnotationDecorator(String name, Annotation annotation) {
    super(name);
    this.annotation = annotation;
   }
  @Override
  public void andThenVisit(ObjectMetaBuilder builder) {
    builder.addToAnnotations(annotation.getKey(), annotation.getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AddAnnotationDecorator that = (AddAnnotationDecorator) o;
    return Objects.equals(annotation, that.annotation);
  }

  @Override
  public int hashCode() {

    return Objects.hash(annotation);
  }
}
