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
package io.ap4k.openshift.handler;

import io.ap4k.Handler;
import io.ap4k.Resources;
import io.ap4k.kubernetes.config.Configuration;
import io.ap4k.deps.openshift.api.model.BuildConfig;
import io.ap4k.deps.openshift.api.model.BuildConfigBuilder;
import io.ap4k.deps.openshift.api.model.ImageStream;
import io.ap4k.deps.openshift.api.model.ImageStreamBuilder;
import io.ap4k.kubernetes.config.Env;
import io.ap4k.openshift.config.EditableS2iConfig;
import io.ap4k.openshift.config.S2iConfig;
import io.ap4k.openshift.decorator.AddBuildEnvDecorator;
import io.ap4k.utils.Images;

public class SourceToImageHandler implements Handler<S2iConfig> {

  private static final String OPENSHIFT = "openshift";
  private static final String IMAGESTREAMTAG = "ImageStreamTag";

  private final Resources resources;

  public SourceToImageHandler(Resources resources) {
    this.resources = resources;
  }

  @Override
  public int order() {
    return 350;
  }

  @Override
  public void handle(S2iConfig config) {
    resources.add(OPENSHIFT, createBuilderImageStream(config));
    resources.add(OPENSHIFT, createBuildConfig(config));
    resources.add(OPENSHIFT, createProjectImageStream());

    for (Env env : config.getEnvVars()) {
      resources.decorate(OPENSHIFT, new AddBuildEnvDecorator(env));
    }
  }

  @Override
  public boolean canHandle(Class<? extends Configuration> type) {
    return type.equals(S2iConfig.class) || type.equals(EditableS2iConfig.class);
  }
  /**
   * Create an {@link ImageStream} for the {@link S2iConfig}.
   * @param config   The config.
   * @return         The build config.
   */
  public ImageStream createBuilderImageStream(S2iConfig config) {
    String repository = Images.getRepository(config.getBuilderImage());

    String name = !repository.contains("/")
      ? repository
      : repository.substring(repository.lastIndexOf("/") + 1);

    return new ImageStreamBuilder()
      .withNewMetadata()
      .withName(name)
      .withLabels(resources.getLabels())
      .endMetadata()
      .withNewSpec()
      .withDockerImageRepository(repository)
      .endSpec()
      .build();
  }


  /**
   * Create an {@link ImageStream} for the {@link S2iConfig}.
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
   * Create a {@link BuildConfig} for the {@link S2iConfig}.
   * @param config   The config.
   * @return          The build config.
   */
  public BuildConfig createBuildConfig(S2iConfig config) {
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
