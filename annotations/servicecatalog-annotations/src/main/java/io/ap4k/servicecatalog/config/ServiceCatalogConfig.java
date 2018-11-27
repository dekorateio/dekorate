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
package io.ap4k.servicecatalog.config;

import io.ap4k.kubernetes.config.ConfigKey;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.project.Project;
import io.sundr.builder.annotations.Buildable;
import io.sundr.builder.annotations.BuildableReference;

import java.util.List;
import java.util.Map;

@Buildable(builderPackage = "io.ap4k.deps.kubernetes.api.builder", refs = @BuildableReference(Configuration.class))
public class ServiceCatalogConfig extends Configuration {

  private final List<ServiceCatalogInstance> instances;

  public ServiceCatalogConfig(Project project, Map<ConfigKey, Object> attributes, List<ServiceCatalogInstance> instances) {
    super(project, attributes);
    this.instances = instances;
  }

  public List<ServiceCatalogInstance> getInstances() {
    return instances;
  }
}
