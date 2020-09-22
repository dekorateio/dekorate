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

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import io.dekorate.BuildServiceFactories;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.utils.Serialization;

public interface WithImageConfig extends WithProject {

  String CONFIG_YML = "%s.yml";
  String CONFIG_DIR = "config";

  default <C extends ImageConfiguration> Stream<C> stream(Class<C> type) {
    final Project project = getProject();
    final Path configDir = project.getBuildInfo().getClassOutputDir().resolve(project.getDekorateMetaDir())
        .resolve(CONFIG_DIR);

    return BuildServiceFactories.names()
        .stream()
        .map(n -> String.format(CONFIG_YML, n))
        .map(s -> configDir.resolve(s))
        .filter(p -> p.toFile().exists())
        .map(p -> Serialization.unmarshal(p.toFile(), ImageConfiguration.class))
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
