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

import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.api.builder.VisitableBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.dekorate.utils.Generics;
import io.dekorate.utils.Strings;

import java.util.Optional;

import static io.dekorate.utils.Metadata.getMetadata;

public abstract class NamedResourceDecorator<T> extends Decorator<VisitableBuilder> {
  /**
   * For resource name null acts as a wildcards.
   * Let's use a constant instead, for clarity's shake
   */
  public static final String ANY = null;

  protected final String name;

  private final ResourceVisitor visitor = new ResourceVisitor(null);

  public NamedResourceDecorator() {
    this(ANY);
  }

  public NamedResourceDecorator(String name) {
    this.name = name;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (!objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(name)) {
      builder.accept(visitor.withMetadata(objectMeta.get()));
    } else if (objectMeta.map(m -> m.getName()).filter(s -> s.equals(name)).isPresent()) {
      builder.accept(visitor.withMetadata(objectMeta.get()));
    }
  }

  /**
   * Visit a part of a Resource.
   * @param item the visited item
   * @param the {@link ObjectMeta} of the current resource.
   */
  public abstract void andThenVisit(T item, ObjectMeta resourceMeta);

  private class ResourceVisitor extends TypedVisitor<T> {

    private final ObjectMeta metadata;

    public ResourceVisitor(ObjectMeta metadata) {
      this.metadata = metadata;
    }
    
    @Override
    public void visit(T item) {
      andThenVisit(item, metadata);
    }

    public ResourceVisitor withMetadata(ObjectMeta metadata) {
      return new ResourceVisitor(metadata);
    }

    public Class<T> getType() {
      return (Class)Generics.getTypeArguments(NamedResourceDecorator.class, NamedResourceDecorator.this.getClass()).get(0);
    }
  }
}
