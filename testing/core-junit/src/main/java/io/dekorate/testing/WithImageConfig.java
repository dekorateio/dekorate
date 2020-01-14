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

import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import io.dekorate.BuildServiceFactories;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.utils.Serialization;

public interface WithImageConfig extends WithProject {

  String DEKORATE_ROOT = "META-INF/dekorate";
  String CONFIG_PATH = DEKORATE_ROOT + "/.config/%s.yml";

  default <C extends ImageConfiguration> Stream<C> stream(Class<C> type) {
    return BuildServiceFactories.names()
      .stream()
      .map(n -> String.format(CONFIG_PATH, n))
      .map(r -> WithImageConfig.class.getClassLoader().getResource(r))
      .filter(u -> u != null)
      .map(u -> Serialization.unmarshal(u, ImageConfiguration.class))
      .filter(BuildServiceFactories.configMatches(getProject()))
      .filter(i -> type.isInstance(i))
      .map(i -> (C) i);
  }

  default boolean hasImageConfig() {
    return stream(ImageConfiguration.class).findAny().isPresent();
  }

  default Optional<ImageConfiguration> getImageConfig() {
    return stream(ImageConfiguration.class).findFirst();
  }
}
