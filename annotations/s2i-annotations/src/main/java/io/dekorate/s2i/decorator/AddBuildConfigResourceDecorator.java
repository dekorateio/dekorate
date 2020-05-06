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

package io.dekorate.s2i.decorator;

import io.dekorate.deps.kubernetes.api.model.KubernetesListBuilder;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;
import io.dekorate.deps.openshift.api.model.BuildConfigBuilder;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.RemoveLabelDecorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;

@Description("Add a BuildConfig resource to the list of generated resources.")
public class AddBuildConfigResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private static final String IMAGESTREAMTAG = "ImageStreamTag";
  private static final String LATEST = "latest";

  private S2iBuildConfig config;

  public AddBuildConfigResourceDecorator(S2iBuildConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);

    String repository = Images.getRepository(config.getBuilderImage());
    String builderRepository = Images.getRepository(config.getBuilderImage());
    String builderTag = Images.getTag(config.getBuilderImage());

    String builderName = !builderRepository.contains("/")
      ? builderRepository
      : builderRepository.substring(builderRepository.lastIndexOf("/") + 1);

    //First we need to consult the labels
    String fallbackVersion = Strings.isNotNullOrEmpty(config.getVersion()) ? config.getVersion() : LATEST;
    String version = meta.getLabels().getOrDefault(Labels.VERSION, fallbackVersion);

    list.addToItems(new BuildConfigBuilder()
      .withNewMetadata()
      .withName(config.getName())
      .withLabels(meta.getLabels())
      .endMetadata()
      .withNewSpec()
      .withNewOutput()
      .withNewTo()
      .withKind(IMAGESTREAMTAG)
      .withName(config.getName() + ":" + version)
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
      .endSpec());
  }

  @Override
  public Class<? extends Decorator>[] after() {
    return new Class[] { ResourceProvidingDecorator.class, AddLabelDecorator.class, RemoveLabelDecorator.class };
  }
}
