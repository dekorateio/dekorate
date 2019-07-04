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

import io.dekorate.deps.kubernetes.api.model.PodSpecBuilder;
import io.dekorate.deps.kubernetes.api.model.Container;
import io.sundr.transform.annotations.VelocityTransformation;

@VelocityTransformation("/templates/fluent-visitor.vm")
public class ContainerDecorator implements FluentDecorator<Container, PodSpecBuilder> {

  public static ContainerFluentVisitor createNew() {
    return new ContainerFluentVisitor(v -> new Decorator<PodSpecBuilder>() {
        @Override
        public void visit(PodSpecBuilder b) {
          b.addToContainers(v);
        }
      });
  }

  public static ContainerFluentVisitor createNewInit() {
    return new ContainerFluentVisitor(v -> new Decorator<PodSpecBuilder>() {
        @Override
        public void visit(PodSpecBuilder b) {
          b.addToInitContainers(v);
        }
      });
  }
}
