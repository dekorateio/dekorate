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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.dekorate.kubernetes.config.Annotation;
import io.dekorate.deps.kubernetes.api.model.Pod;
import io.dekorate.deps.kubernetes.api.model.PodBuilder;
import io.dekorate.deps.kubernetes.api.model.Service;
import io.dekorate.deps.kubernetes.api.model.ServiceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddAnnotationDecoratorTest {

  @Test
  public void shouldAddAnnotationToPod() {
    assertEquals("Pod", new PodBuilder().getKind());

    Pod expected = new PodBuilder()
      .withNewMetadata()
      .withName("pod")
      .addToAnnotations("key1","value1")
      .endMetadata()
      .build();

    Pod actual = new PodBuilder()
        .withNewMetadata()
        .withName("pod")
        .endMetadata()
      .accept(new AddAnnotationDecorator(new Annotation("key1", "value1", new String[0])))
        .build();

    assertEquals(expected, actual);
  }

  @Test
  public void shouldAddAnnotationToKind() {
    assertEquals("Pod", new PodBuilder().getKind());

    Pod expectedPod = new PodBuilder()
      .withNewMetadata()
      .withName("pod")
      .addToAnnotations("key1","value1")
      .endMetadata()
      .build();

    Service expectedService = new ServiceBuilder()
      .withNewMetadata()
      .withName("my-service")
      .endMetadata()
      .build();


    Pod actualPod = new PodBuilder()
        .withNewMetadata()
        .withName("pod")
        .endMetadata()
      .accept(new AddAnnotationDecorator(null, new Annotation("key1", "value1", new String[]{"Pod"})))
        .build();

    assertEquals(expectedPod, actualPod);

    Service actualService = new ServiceBuilder()
        .withNewMetadata()
        .withName("my-service")
        .endMetadata()
      .accept(new AddAnnotationDecorator(null, new Annotation("key1", "value1", new String[]{"Pod"})))
        .build();

    assertEquals(expectedService, actualService);
  }
}
