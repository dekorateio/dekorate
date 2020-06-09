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

package io.dekorate.s2i.handler;

import io.dekorate.AbstractKubernetesHandler;
import io.dekorate.Configurators;
import io.dekorate.Handler;
import io.dekorate.HandlerFactory;
import io.dekorate.Logger;
import io.dekorate.LoggerFactory;
import io.dekorate.Resources;
import io.dekorate.WithProject;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.s2i.annotation.S2iBuild;
import io.dekorate.s2i.config.EditableS2iBuildConfig;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.s2i.decorator.AddBuildEnvDecorator;
import io.dekorate.s2i.decorator.AddBuildConfigResourceDecorator;
import io.dekorate.s2i.decorator.AddOutputImageStreamResourceDecorator;
import io.dekorate.s2i.decorator.AddBuilderImageStreamResourceDecorator;
import io.dekorate.utils.Images;

public class S2iHanlder implements Handler<S2iBuildConfig>, HandlerFactory, WithProject {

  private static final String OPENSHIFT = "openshift";
  private final Logger LOGGER = LoggerFactory.getLogger();
  private final Resources resources;

  public S2iHanlder(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 1301;
  }

  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(S2iBuildConfig.class) ||
      type.equals(EditableS2iBuildConfig.class);
  }

  @Override
  public Handler create(Resources resources, Configurators configurators) {
    return new S2iHanlder(resources);
  }

  public void handle(S2iBuildConfig config) {
    if (config.isEnabled()) {
      LOGGER.info("Processing s2i configuration.");
      //TODO: We are temporarily limit S2i to openshift until we find a better way to handle this (#367).
      resources.decorate(OPENSHIFT, new AddBuilderImageStreamResourceDecorator(config));
      resources.decorate(OPENSHIFT, new AddOutputImageStreamResourceDecorator(config));
      resources.decorate(OPENSHIFT, new AddBuildConfigResourceDecorator(config));

      for (Env env : config.getBuildEnvVars()) {
        resources.decorate(new AddBuildEnvDecorator(env));
      }
    }
  }
}
