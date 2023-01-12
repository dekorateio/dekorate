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

package io.dekorate.kubernetes.decorator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.dekorate.ConfigReference;
import io.dekorate.WithConfigReferences;
import io.dekorate.doc.Description;
import io.dekorate.kubernetes.config.KubernetesConfig;
import io.dekorate.utils.Strings;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;

@Description("Add an ingress to the list.")
public class AddIngressDecorator extends ResourceProvidingDecorator<KubernetesListBuilder> implements WithConfigReferences {

  private final KubernetesConfig config;
  private final Map<String, String> allLabels; //A combination of config and project labels.

  public AddIngressDecorator(KubernetesConfig config, Map<String, String> allLabels) {
    this.config = config;
    this.allLabels = allLabels;
  }

  public void visit(KubernetesListBuilder list) {
    if (!config.getIngress().isExpose()) {
      return;
    }

    if (contains(list, ANY, "Ingress", config.getName())) {
      return;
    }
    list.addToItems(new IngressBuilder()
        .withNewMetadata()
        .withName(config.getName())
        .withLabels(allLabels)
        .endMetadata()
        .withNewSpec()
        .withIngressClassName(Strings.defaultIfEmpty(config.getIngress().getIngressClassName(), null))
        .addNewRule()
        .withHost(config.getIngress().getHost())
        .endRule()
        .endSpec()
        .build());
  }

  @Override
  public List<ConfigReference> getConfigReferences() {
    if (!config.getIngress().isExpose()) {
      return Collections.emptyList();
    }

    return Arrays.asList(buildConfigReferenceHost());
  }

  private ConfigReference buildConfigReferenceHost() {
    String property = "host";
    String path = "(kind == Ingress && metadata.name == " + config.getName() + ").spec.rules.host";
    return new ConfigReference(property, path, config.getIngress().getHost());
  }
}
