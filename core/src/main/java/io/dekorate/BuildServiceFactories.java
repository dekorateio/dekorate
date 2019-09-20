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

package io.dekorate;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;

public class BuildServiceFactories {

  public static Optional<BuildServiceFactory> find(Project project, ImageConfiguration config) {
    ServiceLoader<BuildServiceFactory> loader = ServiceLoader.load(BuildServiceFactory.class, BuildServiceFactory.class.getClassLoader());
    return StreamSupport.stream(loader.spliterator(), false)
      .filter(f -> f.isApplicable(project, config))
      .sorted()
      .findFirst();
  }
}
