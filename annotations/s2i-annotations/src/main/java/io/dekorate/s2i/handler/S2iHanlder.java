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
import io.dekorate.deps.openshift.api.model.BuildConfig;
import io.dekorate.deps.openshift.api.model.BuildConfigBuilder;
import io.dekorate.deps.openshift.api.model.ImageStream;
import io.dekorate.deps.openshift.api.model.ImageStreamBuilder;
import io.dekorate.kubernetes.config.Configuration;
import io.dekorate.kubernetes.config.Env;
import io.dekorate.s2i.annotation.S2iBuild;
import io.dekorate.s2i.config.EditableS2iBuildConfig;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.s2i.decorator.AddBuildEnvDecorator;
import io.dekorate.utils.Images;

public class S2iHanlder implements Handler<S2iBuildConfig>, HandlerFactory, WithProject {

  private static final String OPENSHIFT = "openshift";
  private static final String IMAGESTREAMTAG = "ImageStreamTag";
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
      resources.add(OPENSHIFT, createBuilderImageStream(config));
      resources.add(OPENSHIFT, createProjectImageStream());
      resources.add(OPENSHIFT, createBuildConfig(config));

      for (Env env : config.getBuildEnvVars()) {
        resources.decorate(new AddBuildEnvDecorator(env));
      }
    }
  }

  /**
   * Create an {@link ImageStream} for the {@link S2iBuildConfig}.
   * @param config   The config.
   * @return         The build config.
   */
  public ImageStream createBuilderImageStream(S2iBuildConfig config) {
    String repository = Images.getRepository(config.getBuilderImage());

    String name = !repository.contains("/")
      ? repository
      : repository.substring(repository.lastIndexOf("/") + 1);

    String dockerImageRepo = Images.removeTag(config.getBuilderImage());

    return new ImageStreamBuilder()
      .withNewMetadata()
      .withName(name)
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withDockerImageRepository(dockerImageRepo)
      .endSpec()
      .build();
  }


  /**
   * Create an {@link ImageStream} for the {@link S2iBuildConfig}.
   * @return         The build config.
   */
  public ImageStream createProjectImageStream() {
    return new ImageStreamBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .build();
  }

  /**
   * Create a {@link BuildConfig} for the {@link S2iBuildConfig}.
   * @param config   The config.
   * @return          The build config.
  */
  public BuildConfig createBuildConfig(S2iBuildConfig config) {
    String builderRepository = Images.getRepository(config.getBuilderImage());
    String builderTag = Images.getTag(config.getBuilderImage());

    String builderName = !builderRepository.contains("/")
      ? builderRepository
      : builderRepository.substring(builderRepository.lastIndexOf("/") + 1);


    return new BuildConfigBuilder()
      .withNewMetadata()
      .withName(resources.getName())
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withNewOutput()
      .withNewTo()
      .withKind(IMAGESTREAMTAG)
      .withName(resources.getName() + ":" + resources.getVersion())
      .endTo()
      .endOutput()
      .withNewSource()
      .withNewBinary()
      .endBinary()
      .endSource()
      .withNewStrategy()
      .withNewSourceStrategy()
      .withEnv()
      .withNewFrom()
      .withKind(IMAGESTREAMTAG)
      .withName(builderName + ":" + builderTag)
      .endFrom()
      .endSourceStrategy()
      .endStrategy()
      .endSpec()
      .build();
  }
}
