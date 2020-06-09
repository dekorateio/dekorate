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

import java.util.Collection;

import io.dekorate.config.ConfigurationSupplier;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;

public interface BuildServiceFactory extends Comparable<BuildServiceFactory> {

  int order();

  String name();
  
  BuildServiceApplicablility checkApplicablility(Project project, ImageConfiguration config);

  BuildServiceApplicablility checkApplicablility(Project project, ConfigurationSupplier<ImageConfiguration> supplier);

  BuildService create(Project project, ImageConfiguration config);
  
  BuildService create(Project project, ImageConfiguration config, Collection<HasMetadata> resources);

	default int compareTo(BuildServiceFactory o) {
		return order() - o.order();
	}
}
