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

package io.dekorate.s2i;

import java.util.Collection;
import java.util.Collections;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceFactory;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;

public class S2iBuildServiceFactory implements BuildServiceFactory {

	@Override
	public boolean isApplicable(Project project, ImageConfiguration config) {
    return true;
	}

	@Override
	public BuildService create(Project project, ImageConfiguration config) {
		return new S2iBuildService(project, config, Collections.emptyList());
	}

	@Override
	public BuildService create(Project project, ImageConfiguration config, Collection<HasMetadata> resources) {
		return new S2iBuildService(project, config, resources);
	}

	@Override
	public int order() {
		return 20;
	}

}
