/**
 * Copyright 2018 The original authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package io.dekorate.halkyon.handler;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.halkyon.config.CapabilityConfig;
import io.dekorate.halkyon.config.CapabilityConfigBuilder;
import io.dekorate.halkyon.config.EditableCapabilityConfig;
import io.dekorate.halkyon.model.Capability;
import io.dekorate.halkyon.model.CapabilityBuilder;
import io.dekorate.halkyon.model.Parameter;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.utils.Strings;

public class CapabilityHandler implements HandlerFactory, Handler<CapabilityConfig> {
  
  private final Logger LOGGER = LoggerFactory.getLogger();
  private final Resources resources;
  
  public Handler create(Resources resources, Configurators configurators) {
    return new CapabilityHandler(resources);
  }
  
  public CapabilityHandler() {
    this(new Resources());
    
  }
  
  public CapabilityHandler(Resources resources) {
    this.resources = resources;
  }
  
  @Override
  public int order() {
    return 1200;
  }
  
  @Override
  public void handle(CapabilityConfig config) {
    LOGGER.info("Processing capability config.");
    if (!Strings.isNullOrEmpty(config.getCategory()) && !Strings.isNullOrEmpty(config.getType())) {
      resources.addCustom(ResourceGroup.NAME, createCapability(config));
    }
  }
  
  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(CapabilityConfig.class) ||
      type.equals(EditableCapabilityConfig.class);
  }
  
  /**
   * Create a {@link Capability} from a {@link CapabilityConfig}.
   *
   * @param config The config.
   * @return The link.
   */
  private Capability createCapability(CapabilityConfig config) {
    return new CapabilityBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withCategory(config.getCategory())
      .withType(config.getType())
      .withVersion(config.getVersion())
      .addAllToParameters(Arrays.stream(config.getParameters())
        .map(p -> new Parameter(p.getName(), p.getValue()))
        .collect(Collectors.toList()))
      
      .endSpec()
      .build();
  }
  
  @Override
  public ConfigurationSupplier<CapabilityConfig> getFallbackConfig() {
    return new ConfigurationSupplier<CapabilityConfig>(new CapabilityConfigBuilder());
  }
  
}
