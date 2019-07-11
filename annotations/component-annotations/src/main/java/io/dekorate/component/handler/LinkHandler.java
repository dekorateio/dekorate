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
 */
package io.dekorate.component.handler;

import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Resources;
import io.dekorate.component.config.EditableLinkConfig;
import io.dekorate.component.config.LinkConfigBuilder;
import io.dekorate.component.config.LinkConfig;
import io.dekorate.component.model.Link;
import io.dekorate.component.model.LinkBuilder;
import io.dekorate.component.model.LinkFluent;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.config.ConfigurationSupplier;
import io.dekorate.utils.Strings;

public class LinkHandler implements HandlerFactory, Handler<LinkConfig> {
  private final Resources resources;

  public Handler create(Resources resources) {
    return new LinkHandler(resources);
  }

  // Used in HandlerFactory
  public LinkHandler() {
    this(new Resources());
  }

  public LinkHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 1300;
  }

  @Override
  public void handle(LinkConfig config) {
    if (Strings.isNullOrEmpty(resources.getName())) {
      resources.setName(config.getName());
    }
    if (!Strings.isNullOrEmpty(config.getComponentName())) {
      resources.addCustom(ResourceGroup.NAME, createLink(config));
    }
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(LinkConfig.class) ||
      type.equals(EditableLinkConfig.class);
  }

  /**
   * Create a {@link Link} from a {@link LinkConfig}.
   *
   * @param config The config.
   * @return The link.
   */
  private Link createLink(LinkConfig config) {
    final LinkFluent.SpecNested<LinkBuilder> linkSpec = new LinkBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .endMetadata()
      .withNewSpec()
      .withComponentName(config.getComponentName())
      .withKind(config.getKind())
      .withNewRef(config.getRef());
    for (Env env : config.getEnvs()) {
      linkSpec.addNewEnv(env.getName(), env.getValue());
    }
    return linkSpec
      .endSpec()
      .build();
  }

  @Override
  public ConfigurationSupplier<LinkConfig> getFallbackConfig() {
    return new ConfigurationSupplier<LinkConfig>(new LinkConfigBuilder());
  }
  
}
