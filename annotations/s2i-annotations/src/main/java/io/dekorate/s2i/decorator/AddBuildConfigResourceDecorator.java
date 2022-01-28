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

import static io.dekorate.ConfigReference.generateConfigReferenceName;

import java.util.Arrays;
import java.util.List;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.AddLabelDecorator;
import io.dekorate.kubernetes.decorator.Decorator;
import io.dekorate.kubernetes.decorator.RemoveLabelDecorator;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.utils.Images;
import io.dekorate.utils.Labels;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.BuildConfigBuilder;

@Description("Add a BuildConfig resource to the list of generated resources.")
public class AddBuildConfigResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder>
    implements WithConfigReferences {

  private static final String IMAGESTREAMTAG = "ImageStreamTag";
  private static final String LATEST = "latest";

  private S2iBuildConfig config;

  public AddBuildConfigResourceDecorator(S2iBuildConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);

    String builderTag = Images.getTag(config.getBuilderImage());
    String builderName = getImageStreamName();

    //First we need to consult the labels
    String fallbackVersion = Strings.isNotNullOrEmpty(config.getVersion()) ? config.getVersion() : LATEST;
    String version = meta.getLabels() != null ? meta.getLabels().getOrDefault(Labels.VERSION, fallbackVersion)
        : fallbackVersion;

    if (contains(list, "build.openshift.io/v1", "BuildConfig", config.getName())) {
      return;
    }

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

  @Override
  public List<ConfigReference> getConfigReferences() {
    return Arrays.asList(buildConfigReferenceTag());
  }

  private ConfigReference buildConfigReferenceTag() {
    String property = generateConfigReferenceName("tag", config.getName(), getImageStreamName());
    String jsonPath = "$.[?(@.kind == 'BuildConfig' && @.metadata.name == '" + config.getName()
        + "')].spec.strategy.sourceStrategy.from.name";

    return new ConfigReference(property, jsonPath);
  }

  private String getImageStreamName() {
    String repository = Images.getRepository(config.getBuilderImage());

    return !repository.contains("/")
        ? repository
        : repository.substring(repository.lastIndexOf("/") + 1);
  }
}
