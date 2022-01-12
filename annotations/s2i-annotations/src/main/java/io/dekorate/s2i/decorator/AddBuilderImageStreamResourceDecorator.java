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

import io.dekorate.WithConfigReference;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.dekorate.utils.Images;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.ImageStreamBuilder;

@Description("Add a builder ImageStream resource to the list of generated resources.")
public class AddBuilderImageStreamResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder>
    implements WithConfigReference {

  private S2iBuildConfig config;

  public AddBuilderImageStreamResourceDecorator(S2iBuildConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getMandatoryDeploymentMetadata(list);

    String name = getImageStreamName();

    if (contains(list, "image.openshift.io/v1", "ImageStream", name)) {
      return;
    }

    String dockerImageRepo = Images.removeTag(config.getBuilderImage());

    list.addToItems(new ImageStreamBuilder()
        .withNewMetadata()
        .withName(name)
        .withLabels(meta.getLabels())
        .endMetadata()
        .withNewSpec()
        .withDockerImageRepository(dockerImageRepo)
        .endSpec());
  }

  @Override
  public String getConfigReference() {
    return generateConfigReferenceName("builder-image", config.getName(), getImageStreamName());
  }

  @Override
  public String getJsonPathProperty() {
    return "$.[?(@.kind == 'ImageStream' && @.metadata.name == '" + getImageStreamName() + "')].spec.dockerImageRepository";
  }

  @Override
  public Object getConfigValue() {
    return Images.removeTag(config.getBuilderImage());
  }

  private String getImageStreamName() {
    String repository = Images.getRepository(config.getBuilderImage());

    return !repository.contains("/")
        ? repository
        : repository.substring(repository.lastIndexOf("/") + 1);
  }
}
