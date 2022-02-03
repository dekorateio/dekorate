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

import java.util.List;

import io.dekorate.doc.Description;
import io.dekorate.kubernetes.decorator.ResourceProvidingDecorator;
import io.dekorate.s2i.config.S2iBuildConfig;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.openshift.api.model.ImageStreamBuilder;

@Description("Add a output ImageStream resource to the list of generated resources.")
public class AddOutputImageStreamResourceDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> {

  private final S2iBuildConfig config;

  public AddOutputImageStreamResourceDecorator(S2iBuildConfig config) {
    this.config = config;
  }

  public void visit(KubernetesListBuilder list) {
    ObjectMeta meta = getDeploymentMetadata(list, this.config.getName()).orElseGet(ObjectMeta::new);

    if (contains(list, "image.openshift.io/v1", "ImageStream", config.getName())) {
      return;
    }

    list.addToItems(new ImageStreamBuilder()
        .withNewMetadata()
        .withName(config.getName())
        .withLabels(meta.getLabels())
        .endMetadata()
        .withNewSpec()
        .endSpec());
  }

}
