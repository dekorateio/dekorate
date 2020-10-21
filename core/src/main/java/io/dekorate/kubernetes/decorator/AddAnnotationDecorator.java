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
import java.util.Objects;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.Annotation;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;

/**
 * A decorator that adds an annotation to the matching resources (filtered by name and/or kinds).
 */
@Description("Add an annotation to the matching resources (filtered by name and/or kinds).")
public class AddAnnotationDecorator extends NamedResourceDecorator<ObjectMetaBuilder> {

  private final Annotation annotation;

  public AddAnnotationDecorator(Annotation annotation) {
    this(ANY, annotation);
  }

  public AddAnnotationDecorator(String name, Annotation annotation) {
    super(ANY, name);
    this.annotation = annotation;
  }

  public AddAnnotationDecorator(String name, String key, String value, String... kinds) {
    super(ANY, name);
    this.annotation = new Annotation(key, value, kinds);
  }



  @Override
  public void andThenVisit(ObjectMetaBuilder builder, String kind, ObjectMeta resourceMeta) {
    if (annotation.getKinds() == null || annotation.getKinds().length == 0 || Arrays.asList(annotation.getKinds()).contains(kind)) {
      andThenVisit(builder, resourceMeta);
    }
  }


  @Override
  public void andThenVisit(ObjectMetaBuilder builder, ObjectMeta resourceMeta) {
    builder.addToAnnotations(annotation.getKey(), annotation.getValue());
  }

  @Override
  public Class<? extends Decorator>[] before() {
    return new Class[] { RemoveAnnotationDecorator.class };
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, ApplyApplicationContainerDecorator.class, AddSidecarDecorator.class };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    AddAnnotationDecorator that = (AddAnnotationDecorator) o;
    return Objects.equals(annotation, that.annotation);
  }

  @Override
  public int hashCode() {

    return Objects.hash(annotation);
  }
}
