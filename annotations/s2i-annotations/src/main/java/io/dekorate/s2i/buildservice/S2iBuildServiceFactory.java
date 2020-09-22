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

package io.dekorate.s2i.buildservice;

import java.util.Collection;
import java.util.Collections;

import io.dekorate.BuildService;
import io.dekorate.BuildServiceApplicablility;
import io.dekorate.BuildServiceFactory;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.kubernetes.config.ImageConfiguration;
import io.dekorate.project.Project;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.fabric8.kubernetes.api.model.HasMetadata;

public class S2iBuildServiceFactory implements BuildServiceFactory {

  private final String S2I = "s2i";
  private final String MESSAGE_OK = "S2i build service is applicable.";
  private final String MESSAGE_DISABLED = "S2i disabled.";

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

  @Override
  public String name() {
    return S2I;
  }

  @Override
  public BuildServiceApplicablility checkApplicablility(Project project, ImageConfiguration config) {
    if (config instanceof S2iBuildConfig) {
      if (((S2iBuildConfig) config).isEnabled()) {
        return new BuildServiceApplicablility(true, MESSAGE_OK);
      } else {
        return new BuildServiceApplicablility(false, MESSAGE_DISABLED);
      }
    }
    return new BuildServiceApplicablility(true, MESSAGE_OK);
  }

  @Override
  public BuildServiceApplicablility checkApplicablility(Project project, ConfigurationSupplier<ImageConfiguration> supplier) {
    if (supplier.isExplicit()) {
      return new BuildServiceApplicablility(true, MESSAGE_OK);
    }
    return checkApplicablility(project, supplier.get());
  }
}
