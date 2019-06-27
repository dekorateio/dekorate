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
package io.ap4k.kubernetes.decorator;

import io.ap4k.deps.kubernetes.api.builder.TypedVisitor;
import io.ap4k.deps.kubernetes.api.builder.VisitableBuilder;
import io.ap4k.deps.kubernetes.api.model.ObjectMeta;
import io.ap4k.utils.Generics;
import io.ap4k.utils.Strings;

import java.util.Optional;

import static io.ap4k.utils.Metadata.getMetadata;

public abstract class ApplicationResourceDecorator<T> extends Decorator<VisitableBuilder> {
  /**
   * For resource name null acts as a wildcards.
   * Let's use a constant instead, for clarity's shake
   */
  public static final String ANY = null;

  protected final String name;

  private final ResourceVisitor visitor = new ResourceVisitor();

  public ApplicationResourceDecorator(String name) {
    this.name = name;
  }

  @Override
  public void visit(VisitableBuilder builder) {
    Optional<ObjectMeta> objectMeta = getMetadata(builder);
    if (!objectMeta.isPresent()) {
      return;
    }
    if (Strings.isNullOrEmpty(name) || objectMeta.map(m -> m.getName()).filter(s -> s.equals(name)).isPresent()) {
      builder.accept(visitor);
    }
  }

  public abstract void andThenVisit(T item);

  private class ResourceVisitor extends TypedVisitor<T> {

    @Override
    public void visit(T item) {
     andThenVisit(item);
    }

    public Class<T> getType() {
      return (Class)Generics.getTypeArguments(ApplicationResourceDecorator.class, ApplicationResourceDecorator.this.getClass()).get(0);
    }
  }
}
