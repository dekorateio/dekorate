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

package io.dekorate.testing;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.dekorate.utils.Generics;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;

public abstract class DiagnosticsFactory<T extends HasMetadata> {

  public abstract DiagnosticsService<T> create(KubernetesClient client);

  /**
   * The type of {@link DiagnosticsService} this factory handles.
   * 
   * @return the resource type the factory handles.
   */
  public Class<T> getType() {
    return (Class) Generics.getTypeArguments(DiagnosticsFactory.class, getClass()).get(0);
  }

  public static <R extends HasMetadata> Optional<DiagnosticsService<R>> create(KubernetesClient client, Class<R> type) {
    return find(type).map(f -> f.create(client));
  }

  private static Stream<DiagnosticsFactory> stream() {
    return StreamSupport
        .stream(ServiceLoader.load(DiagnosticsFactory.class, DiagnosticsFactory.class.getClassLoader()).spliterator(), false);
  }

  public static <R extends HasMetadata> Optional<DiagnosticsFactory<R>> find(Class<R> type) {
    return stream()
        .filter(f -> f.getType().isAssignableFrom(type))
        .map(f -> (DiagnosticsFactory<R>) f)
        .findFirst();
  }
}
