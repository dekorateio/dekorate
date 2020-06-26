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

package io.dekorate.jib.buildservice;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceApplicablility;
import io.dekorate.BuildServiceFactory;
import io.dekorate.config.ConfigurationSupplier;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.MavenInfoReader;
import io.dekorate.project.Project;

public class JibBuildServiceFactory implements BuildServiceFactory {

  private static final List<String> SUPPORTED_TOOLS = Arrays.asList(MavenInfoReader.MAVEN);
  private static final String JIB="jib";
 
	@Override
	public String name() {
		return JIB;
	}

	@Override
	public int order() {
		return 15;
	}

	@Override
	public BuildServiceApplicablility checkApplicablility(Project project, ImageConfiguration config) {
    boolean supportedTool = SUPPORTED_TOOLS.contains(project.getBuildInfo().getBuildTool());

    if (!supportedTool) {
      return new BuildServiceApplicablility(false, "Project build tool no support by Jib");
    } else {
      return new BuildServiceApplicablility(true, "ok");
    }
	}

	@Override
	public BuildService create(Project project, ImageConfiguration config) {
		return new JibBuildService(project, config);
	}

	@Override
	public BuildService create(Project project, ImageConfiguration config, Collection<HasMetadata> resources) {
		return new JibBuildService(project, config);
	}

	@Override
	public BuildServiceApplicablility checkApplicablility(Project project, ConfigurationSupplier<ImageConfiguration> supplier) {
    if (supplier.isExplicit()) {
      return new BuildServiceApplicablility(true, "Jib has been explicitly configured!");
    }
    return checkApplicablility(project, supplier.get());
	}
}
